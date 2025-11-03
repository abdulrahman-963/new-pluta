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

    def create_legend(self, output_dir):
        """Create a legend image explaining the colors and labels"""
        legend_img = np.ones((300, 500, 3), dtype=np.uint8) * 255

        # Title
        cv2.putText(legend_img, "Detection Legend", (150, 30),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 0), 2)

        # Define colors and positions
        colors = {
            "Dining Table": (0, 255, 0),    # Green
            "Chair": (255, 0, 0),           # Blue
            "Bench": (0, 255, 255),         # Yellow
            "Couch": (255, 0, 255),         # Magenta
            "Person": (255, 255, 0)         # Cyan
        }

        y_pos = 60
        for class_name, color in colors.items():
            # Draw rectangle
            cv2.rectangle(legend_img, (50, y_pos), (100, y_pos + 30), color, 2)
            # Draw text
            cv2.putText(legend_img, class_name, (120, y_pos + 20),
                       cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)
            y_pos += 40

        # Note
        cv2.putText(legend_img, "Each object type has a unique color", (50, 270),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0), 1)

        legend_path = os.path.join(output_dir, "legend.jpg")
        cv2.imwrite(legend_path, legend_img)
        return legend_path

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

            # Create legend
            legend_path = self.create_legend(output_dir)

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
                "image_info": {
                    "filename": os.path.basename(image_path),
                    "resolution": f"{width}x{height}",
                    "file_path": image_path
                },
                "detection_summary": {
                    "dining_table": len(detected_objects["dining table"]),
                    "chair": len(detected_objects["chair"]),
                    "bench": len(detected_objects["bench"]),
                    "couch": len(detected_objects["couch"]),
                    "person": len(detected_objects["person"]),
                    "total_objects": sum(len(boxes) for boxes in detected_objects.values())
                },
                "bounding_boxes": detected_objects,
                "table_seating_relationships": table_chair_relationships,
                "output_files": {
                    "annotated_image": annotated_path,
                    "legend_file": legend_path,
                    "output_directory": output_dir
                },
                "debug_info": debug_info,
                "analysis_metadata": {
                    "model_used": "YOLOv8x",
                    "confidence_threshold": self.confidence_threshold,
                    "target_classes": self.TARGET_CLASSES,
                    "preprocessing_applied": "Histogram Equalization + CLAHE"
                },
                "status": "success"
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

        if args.output_json:
            # Process for service integration
            results = detector.process_image_for_service(args.image_path)

            # Output JSON to stdout for service to capture
            print(json.dumps(results, indent=2))

            # Exit with appropriate code
            if results.get("status") == "success":
                sys.exit(0)
            else:
                sys.exit(1)
        else:
            # Original processing for standalone use
            results = detector.process_image_for_service(args.image_path)

            if results.get("status") == "success":
                print(json.dumps(results, indent=2))
                """
                print("" + "="*60)
                print("OBJECT DETECTION RESULTS")
                print("="*60)
                print(json.dumps(results, indent=2))
                print("="*60)

                # Print summary
                summary = results["detection_summary"]
                print(f"SUMMARY for {results['image_info']['filename']}:")
                print(f"Dining Tables: {summary['dining_table']}")
                print(f"Chairs: {summary['chair']}")
                print(f"Benches: {summary['bench']}")
                print(f"Couches: {summary['couch']}")
                print(f"Persons: {summary['person']}")
                print(f"Total Objects: {summary['total_objects']}")
                """
                if results['table_seating_relationships']:
                    print(f"\nTable-Seating Details:")
                    for table_info in results['table_seating_relationships']:
                        print(f"  Table {table_info['table_id']}: {table_info['total_seating']} total seating "
                              f"({table_info['chairs_count']} chairs, {table_info['benches_count']} benches, "
                              f"{table_info['couches_count']} couches)")

                print(f"\nOutput files saved to: {results['output_files']['output_directory']}")
            else:
                print(f"Error: {results.get('error_message', 'Unknown error')}")
                sys.exit(1)

    except Exception as e:
        error_result = {
            "status": "error",
            "error_message": str(e),
            "image_path": args.image_path
        }

        if args.output_json:
            print(json.dumps(error_result, indent=2))
        else:
            print(f"Error: {e}")

        sys.exit(1)

if __name__ == "__main__":
    main()