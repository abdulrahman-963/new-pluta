import cv2
import numpy as np
from ultralytics import YOLO
import argparse
from collections import defaultdict
import json
import os
import sys

class TableChairDetector:
    def __init__(self, model_path='/Users/abdulrahman/Desktop/POC/CameraAi/yolo-analyzer/yolov8x.pt', confidence_threshold=0.2):
        """
        Initialize the detector with YOLOv8x model (best accuracy)

        Args:
            model_path: Path to YOLOv8 model weights (yolov8x.pt for best performance)
            confidence_threshold: Minimum confidence for detections
        """
        self.model = YOLO(model_path)
        self.confidence_threshold = confidence_threshold

        # Target classes for detection
        self.TARGET_CLASSES = ["chair", "dining table", "bench", "couch", "person"]

        # COCO class IDs for target objects
        self.class_ids = {
            "chair": 56,
            "dining table": 60,
            "bench": 13,
            "couch": 57,
            "person": 0
        }

    def preprocess_image(self, image):
        """
        Preprocess image for better detection

        Args:
            image: Input image (BGR or grayscale)

        Returns:
            processed_image: Enhanced image for detection
        """
        # Convert to grayscale if it's color
        if len(image.shape) == 3:
            gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        else:
            gray = image.copy()

        # Apply histogram equalization for better contrast
        equalized = cv2.equalizeHist(gray)

        # Apply CLAHE (Contrast Limited Adaptive Histogram Equalization)
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
        enhanced = clahe.apply(gray)

        # Combine original and enhanced for better results
        combined = cv2.addWeighted(gray, 0.5, enhanced, 0.5, 0)

        # Convert back to 3-channel for YOLO
        processed_image = cv2.cvtColor(combined, cv2.COLOR_GRAY2BGR)

        return processed_image

    def calculate_distance(self, box1, box2):
        """Calculate distance between centers of two bounding boxes"""
        center1 = ((box1[0] + box1[2]) / 2, (box1[1] + box1[3]) / 2)
        center2 = ((box2[0] + box2[2]) / 2, (box2[1] + box2[3]) / 2)
        return np.sqrt((center1[0] - center2[0])**2 + (center1[1] - center2[1])**2)

    def detect_image(self, image):
        """
        Detect target objects in a single image

        Returns:
            detected_objects: Dictionary with lists of bounding boxes for each class
            debug_info: Debug information about detections
        """
        # Preprocess image for better detection
        processed_image = self.preprocess_image(image)

        # Run detection
        results = self.model(processed_image, conf=self.confidence_threshold)

        # Initialize detection storage
        detected_objects = {
            "chair": [],
            "dining table": [],
            "bench": [],
            "couch": [],
            "person": []
        }

        debug_detections = []

        for result in results:
            boxes = result.boxes
            if boxes is not None:
                for box in boxes:
                    class_id = int(box.cls)
                    confidence = float(box.conf)
                    class_name = self.model.names[class_id]

                    debug_detections.append({
                        'class_id': class_id,
                        'class_name': class_name,
                        'confidence': round(confidence, 3)
                    })

                    if confidence >= self.confidence_threshold:
                        # Check if this is one of our target classes
                        if class_name in self.TARGET_CLASSES:
                            x1, y1, x2, y2 = box.xyxy[0].cpu().numpy()
                            bbox = {
                                "x1": int(x1),
                                "y1": int(y1),
                                "x2": int(x2),
                                "y2": int(y2),
                                "confidence": confidence
                            }
                            detected_objects[class_name].append(bbox)

        debug_info = {
            'total_detections': len(debug_detections),
            'target_detections': {class_name: len(boxes) for class_name, boxes in detected_objects.items()},
            'all_detections': debug_detections
        }

        return detected_objects, debug_info

    def count_seating_around_tables(self, detected_objects, proximity_threshold=150):
        """
        Count how many seating objects (chairs, benches, couches) are near each table

        Args:
            detected_objects: Dictionary with detected objects by class
            proximity_threshold: Maximum distance to consider seating belongs to table

        Returns:
            seating_per_table: Dictionary mapping table index to seating counts
        """
        table_boxes = detected_objects["dining table"]
        seating_boxes = detected_objects["chair"] + detected_objects["bench"] + detected_objects["couch"]

        seating_per_table = defaultdict(lambda: {"chairs": 0, "benches": 0, "couches": 0, "total": 0})

        for seating_box in seating_boxes:
            closest_table = None
            min_distance = float('inf')

            for i, table_box in enumerate(table_boxes):
                distance = self.calculate_distance(
                    [seating_box["x1"], seating_box["y1"], seating_box["x2"], seating_box["y2"]],
                    [table_box["x1"], table_box["y1"], table_box["x2"], table_box["y2"]]
                )
                if distance < proximity_threshold and distance < min_distance:
                    min_distance = distance
                    closest_table = i

            if closest_table is not None:
                # Determine seating type
                if seating_box in detected_objects["chair"]:
                    seating_per_table[closest_table]["chairs"] += 1
                elif seating_box in detected_objects["bench"]:
                    seating_per_table[closest_table]["benches"] += 1
                elif seating_box in detected_objects["couch"]:
                    seating_per_table[closest_table]["couches"] += 1
                seating_per_table[closest_table]["total"] += 1

        return dict(seating_per_table)

    def draw_detections(self, image, detected_objects, image_name=""):
        """
        Draw bounding boxes and labels on image

        Args:
            image: Original image
            detected_objects: Dictionary with detected objects by class
            image_name: Image name for labeling

        Returns:
            annotated_image: Image with drawn detections
        """
        annotated_image = image.copy()

        # Define colors for each class
        colors = {
            "dining table": (0, 255, 0),    # Green
            "chair": (255, 0, 0),           # Blue
            "bench": (0, 255, 255),         # Yellow
            "couch": (255, 0, 255),         # Magenta
            "person": (255, 255, 0)         # Cyan
        }

        # Draw each class
        for class_name, boxes in detected_objects.items():
            color = colors[class_name]

            for i, box in enumerate(boxes):
                x1, y1, x2, y2 = box["x1"], box["y1"], box["x2"], box["y2"]
                cv2.rectangle(annotated_image, (x1, y1), (x2, y2), color, 2)

                # Label with object ID
                label = f"{class_name.title()} {i+1}"
                label_size = cv2.getTextSize(label, cv2.FONT_HERSHEY_SIMPLEX, 0.5, 2)[0]
                cv2.rectangle(annotated_image, (x1, y1-20), (x1 + label_size[0], y1), color, -1)
                cv2.putText(annotated_image, label, (x1, y1-3),
                           cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 2)

        # Add image info
        total_objects = sum(len(boxes) for boxes in detected_objects.values())
        info_text = f"{image_name} | Total Objects: {total_objects}"
        cv2.putText(annotated_image, info_text, (10, 30),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

        return annotated_image

    def save_annotated_image(self, image, detected_objects, image_path, output_dir):
        """
        Save annotated image with detections

        Args:
            image: Original image
            detected_objects: Dictionary with detected objects by class
            image_path: Path to original image
            output_dir: Output directory for annotated images
        """
        # Create output directory if it doesn't exist
        os.makedirs(output_dir, exist_ok=True)

        # Get image name without extension
        image_name = os.path.splitext(os.path.basename(image_path))[0]

        # Draw detections
        annotated_image = self.draw_detections(image, detected_objects, image_name)

        # Save annotated image
        output_path = os.path.join(output_dir, f"{image_name}_detections.jpg")
        cv2.imwrite(output_path, annotated_image)

        return output_path

    def process_image_for_service(self, image_path):
        """
        Process a single image and return results in JSON format for service integration

        Args:
            image_path: Path to input image file

        Returns:
            dict: Analysis results in JSON-serializable format
        """
        try:
            # Read image
            image = cv2.imread(image_path)
            if image is None:
                raise ValueError(f"Cannot read image file: {image_path}")

            # Get image properties
            height, width = image.shape[:2]

            # Detect objects
            detected_objects, debug_info = self.detect_image(image)

            # Count seating around tables
            seating_per_table = self.count_seating_around_tables(detected_objects)

            # Create output directory
            image_name = os.path.splitext(os.path.basename(image_path))[0]
            output_dir = os.path.join(os.path.dirname(image_path), f"{image_name}_analysis_results")

            # Save annotated image
            annotated_path = self.save_annotated_image(image, detected_objects, image_path, output_dir)

            # Prepare table-chair relationships for JSON
            table_chair_relationships = []
            for table_id, seating_info in seating_per_table.items():
                table_chair_relationships.append({
                    "table_id": table_id + 1,
                    "chairs_count": seating_info["chairs"],
                    "benches_count": seating_info["benches"],
                    "couches_count": seating_info["couches"],
                    "total_seating": seating_info["total"]
                })

            # Prepare results
            results = {

                 "resolution": f"{width}x{height}",
                 "tablesDetected": len(detected_objects["dining table"]),
                 "chairsDetected": len(detected_objects["chair"]),
                 "benchesDetected": len(detected_objects["bench"]),
                 "couchesDetected": len(detected_objects["couch"]),
                 "personsDetected": len(detected_objects["person"]),
                 "totalDetected": sum(len(boxes) for boxes in detected_objects.values()),
                 "annotatedImagePath": annotated_path,
                 "status": "COMPLETED"
            }

            return results

        except Exception as e:
            return {
                "status": "error",
                "error_message": str(e),
                "image_path": image_path
            }

def main():
    parser = argparse.ArgumentParser(description='Detect objects in images using YOLOv8x for service integration')
    parser.add_argument('image_path', help='Path to input image file')
    parser.add_argument('--model', '-m', default='yolov8x.pt',
                       help='YOLO model (yolov8x.pt for best accuracy, yolov8n.pt for speed)')
    parser.add_argument('--confidence', '-c', type=float, default=0.2,
                       help='Confidence threshold')
    parser.add_argument('--output-json', action='store_true',
                       help='Output results in JSON format for service integration')

    args = parser.parse_args()

    try:
        # Initialize detector
        detector = TableChairDetector(args.model, args.confidence)

        # Always process and output JSON only
        results = detector.process_image_for_service(args.image_path)
        print(json.dumps(results, indent=2))
        if results.get("status") == "COMPLETED":
            sys.exit(0)
        else:
            sys.exit(1)

    except Exception as e:
        error_result = {
            "status": "error",
            "error_message": str(e),
            "image_path": args.image_path
        }
        print(json.dumps(error_result, indent=2))
        sys.exit(1)

if __name__ == "__main__":
    main()