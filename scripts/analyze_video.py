import cv2
import numpy as np
from ultralytics import YOLO
import argparse
from collections import defaultdict
import json
import os
import sys

class CameraZone:
    """Define analysis zones for each camera"""
    def __init__(self, camera_id, zone_polygon, table_id):
        """
        camera_id: unique identifier for camera
        zone_polygon: list of (x, y) points defining polygon zone
        table_id: optional table identifier this camera is responsible for
        """
        self.camera_id = camera_id
        self.zone_polygon = np.array(zone_polygon, dtype=np.int32)
        self.table_id = table_id
        
    def is_in_zone(self, x, y):
        """Check if point (x, y) is inside the zone"""
        result = cv2.pointPolygonTest(self.zone_polygon, (float(x), float(y)), False)
        return result >= 0
    
    def is_bbox_in_zone(self, bbox, threshold=0.5):
        """
        Check if bounding box is in zone
        Args:
            bbox: dict with x1, y1, x2, y2
            threshold: minimum percentage of bbox that must be in zone (0.0 to 1.0)
        Returns:
            bool: True if bbox center or sufficient area is in zone
        """
        percentage = self.get_bbox_zone_percentage(bbox)
        return percentage >= threshold

    def get_bbox_zone_percentage(self, bbox):
        """
        Calculate what percentage of a bounding box is within the zone
        Args:
            bbox: dict with x1, y1, x2, y2
        Returns:
            float: Percentage of bbox in zone (0.0 to 1.0)
        """
        x1, y1, x2, y2 = bbox["x1"], bbox["y1"], bbox["x2"], bbox["y2"]

        # Check center point
        center_x = (x1 + x2) / 2
        center_y = (y1 + y2) / 2

        # Check corners and additional points for partial overlap
        points_to_check = [
            (center_x, center_y),  # center
            (x1, y1), (x2, y1), (x2, y2), (x1, y2),  # corners
            (center_x, y1), (center_x, y2),  # mid points
            (x1, center_y), (x2, center_y)
        ]

        points_in_zone = sum(1 for px, py in points_to_check if self.is_in_zone(px, py))

        return points_in_zone / len(points_to_check)

    def draw_zone(self, image):
        """Draw the zone on image"""
        overlay = image.copy()
        """cv2.fillPoly(overlay, [self.zone_polygon], (0, 255, 0, 30))"""
        cv2.addWeighted(overlay, 0.3, image, 0.7, 0, image)
        cv2.polylines(image, [self.zone_polygon], True, (0, 255, 0), 2)

        # Add label
        x, y = self.zone_polygon[0]
        label = f"Camera {self.camera_id}"
        if self.table_id:
            label += f" - Table {self.table_id}"
        cv2.putText(image, label, (x, y - 10),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 1)


class TableChairDetector:
    def __init__(self, model_path='yolo11x.pt',
    confidence_threshold=0.3, iou_threshold=0.2, proximity_threshold=50, zone_threshold=0.7):
        """
        Initialize the detector with YOLO model

        Args:
            model_path: Path to YOLO model weights
            confidence_threshold: Minimum confidence for detections
            iou_threshold: Minimum IoU to consider a seat occupied
            proximity_threshold: Minimum center distance to consider a seat occupied
            zone_threshold: Minimum percentage of bbox in zone to be counted (0.0 to 1.0)
        """
        self.model = YOLO(model_path)
        self.confidence_threshold = confidence_threshold
        self.iou_threshold = iou_threshold
        self.proximity_threshold = proximity_threshold
        self.zone_threshold = zone_threshold

        # Camera zones configuration
        self.camera_zones = {}

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

    def add_camera_zone(self, camera_id, zone_polygon, table_id=None):
        """
        Add a camera zone to restrict analysis to specific area

        Args:
            camera_id: unique camera identifier (e.g., 'cam1', 'cam2')
            zone_polygon: list of (x, y) tuples defining the polygon
            table_id: optional table identifier this camera monitors
        """
        self.camera_zones[camera_id] = CameraZone(camera_id, zone_polygon, table_id)
        print(f"Added zone for camera {camera_id}" + (f" monitoring table {table_id}" if table_id else ""))

    def filter_detections_by_zone(self, detected_objects, camera_id, zone_threshold=0.7):
        """
        Filter detected objects to only include those within the camera's zone

        Args:
            detected_objects: Dictionary with detected objects by class
            camera_id: Camera identifier
            zone_threshold: Minimum percentage of bbox that must be in zone (0.0 to 1.0)

        Returns:
            filtered_objects: Dictionary with only objects in the zone
        """
        if camera_id not in self.camera_zones:
            # No zone defined, return all detections
            return detected_objects

        zone = self.camera_zones[camera_id]
        filtered_objects = {
            "chair": [],
            "dining table": [],
            "bench": [],
            "couch": [],
            "person": []
        }

        for class_name, boxes in detected_objects.items():
            for box in boxes:
                if zone.is_bbox_in_zone(box, threshold=zone_threshold):
                    filtered_objects[class_name].append(box)

        return filtered_objects

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

    def detect_image(self, image, camera_id=None):
        """
        Detect target objects in a single image

        Args:
            image: Input image
            camera_id: Optional camera identifier to filter by zone

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

                            # Calculate zone percentage if camera_id is specified
                            if camera_id and camera_id in self.camera_zones:
                                zone = self.camera_zones[camera_id]
                                bbox["zone_percentage"] = zone.get_bbox_zone_percentage(bbox)

                            detected_objects[class_name].append(bbox)

        # Filter by camera zone if specified
        if camera_id:
            detected_objects = self.filter_detections_by_zone(detected_objects, camera_id, self.zone_threshold)

        debug_info = {
            'total_detections': len(debug_detections),
            'target_detections': {class_name: len(boxes) for class_name, boxes in detected_objects.items()},
            'all_detections': debug_detections,
            'camera_id': camera_id
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

    def count_occupied_seating(self, detected_objects, seating_type):
        """
        Count how many seating objects (chairs, benches, couches) are occupied by a person.
        """
        seats = detected_objects[seating_type]
        persons = detected_objects["person"]
        occupied = set()

        def iou(boxA, boxB):
            xA = max(boxA["x1"], boxB["x1"])
            yA = max(boxA["y1"], boxB["y1"])
            xB = min(boxA["x2"], boxB["x2"])
            yB = min(boxA["y2"], boxB["y2"])
            interArea = max(0, xB - xA) * max(0, yB - yA)
            boxAArea = (boxA["x2"] - boxA["x1"]) * (boxA["y2"] - boxA["y1"])
            boxBArea = (boxB["x2"] - boxB["x1"]) * (boxB["y2"] - boxB["y1"])
            unionArea = boxAArea + boxBArea - interArea
            if unionArea == 0:
                return 0
            return interArea / unionArea

        def center_distance(boxA, boxB):
            cxA = (boxA["x1"] + boxA["x2"]) / 2
            cyA = (boxA["y1"] + boxA["y2"]) / 2
            cxB = (boxB["x1"] + boxB["x2"]) / 2
            cyB = (boxB["y1"] + boxB["y2"]) / 2
            return np.sqrt((cxA - cxB) ** 2 + (cyA - cyB) ** 2)

        for i, seat in enumerate(seats):
            for person in persons:
                if iou(seat, person) > self.iou_threshold or center_distance(seat, person) < self.proximity_threshold:
                    occupied.add(i)
                    break
        occupied_count = len(occupied)
        unoccupied_count = len(seats) - occupied_count
        return occupied_count, unoccupied_count

    def draw_detections(self, image, detected_objects, camera_id=None, image_name=""):
        """
        Draw bounding boxes and labels on image

        Args:
            image: Original image
            detected_objects: Dictionary with detected objects by class
            camera_id: Optional camera ID to draw zone
            image_name: Image name for labeling

        Returns:
            annotated_image: Image with drawn detections
        """
        annotated_image = image.copy()

        # Draw camera zone if defined
        if camera_id and camera_id in self.camera_zones:
            self.camera_zones[camera_id].draw_zone(annotated_image)

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
                cv2.rectangle(annotated_image, (x1, y1), (x2, y2), color, 1)

                # Label with object ID and zone percentage if available
                label = f"{class_name.title()} {i+1}, {box['confidence']:.2f}"
                if "zone_percentage" in box:
                    zone_pct = int(box["zone_percentage"] * 100)
                    label += f" | Zone: {zone_pct}%"

                label_size = cv2.getTextSize(label, cv2.FONT_HERSHEY_SIMPLEX, 0.4, 1)[0]
                cv2.rectangle(annotated_image, (x1, y1-15), (x1 + label_size[0], y1), color, -1)
                cv2.putText(annotated_image, label, (x1, y1-3),
                           cv2.FONT_HERSHEY_SIMPLEX, 0.4, (255, 255, 255), 1)

        # Add image info
        total_objects = sum(len(boxes) for boxes in detected_objects.values())
        info_text = f"{image_name} | Total Objects: {total_objects}"
        if camera_id:
            info_text = f"Camera {camera_id} | {info_text}"
        cv2.putText(annotated_image, info_text, (10, 30),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

        return annotated_image

    def save_annotated_image(self, image, detected_objects, image_path, output_dir, camera_id=None, table_id=None):
        """
        Save annotated image with detections
        """
        os.makedirs(output_dir, exist_ok=True)
        image_name = os.path.splitext(os.path.basename(image_path))[0]
        annotated_image = self.draw_detections(image, detected_objects, camera_id, image_name)

        output_filename = f"{image_name}_detections.jpg"
        if camera_id and table_id is not None:
            output_filename = f"{camera_id}_table{table_id}_{image_name}_detections.jpg"
        elif camera_id:
            output_filename = f"{camera_id}_{image_name}_detections.jpg"
        elif table_id is not None:
            output_filename = f"table{table_id}_{image_name}_detections.jpg"

        output_path = os.path.join(output_dir, output_filename)
        cv2.imwrite(output_path, annotated_image)
        return output_path

    def process_image_for_service(self, image_path, camera_id=None, show_all_detections=False):
        """
        Process a single image and return results in JSON format

        Args:
            image_path: Path to input image file
            camera_id: Camera identifier for zone filtering
            show_all_detections: If True, show all detected objects in annotated image (even those outside zone)

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

            # Get table_id from camera zone if available
            zone_table_id = None
            if camera_id and camera_id in self.camera_zones:
                zone_table_id = self.camera_zones[camera_id].table_id

            # Detect objects with camera zone filtering
            detected_objects, debug_info = self.detect_image(image, camera_id)

            # For annotation: keep all detections if show_all_detections is True
            all_detected_objects = None
            if show_all_detections and camera_id:
                # Detect again without filtering to get all objects
                all_detected_objects, _ = self.detect_image(image, None)
                # But still add zone percentage for visualization
                if camera_id in self.camera_zones:
                    zone = self.camera_zones[camera_id]
                    for class_name, boxes in all_detected_objects.items():
                        for box in boxes:
                            box["zone_percentage"] = zone.get_bbox_zone_percentage(box)

            # Count seating around tables
            seating_per_table = self.count_seating_around_tables(detected_objects)

            # Count occupied seating for each type
            occupied_chairs, unoccupied_chairs = self.count_occupied_seating(detected_objects, 'chair')
            occupied_benches, unoccupied_benches = self.count_occupied_seating(detected_objects, 'bench')
            occupied_couches, unoccupied_couches = self.count_occupied_seating(detected_objects, 'couch')

            # Count sitting persons
            persons = detected_objects["person"]
            seats = detected_objects["chair"] + detected_objects["bench"] + detected_objects["couch"]
            sitting_person_indices = set()

            def iou(boxA, boxB):
                xA = max(boxA["x1"], boxB["x1"])
                yA = max(boxA["y1"], boxB["y1"])
                xB = min(boxA["x2"], boxB["x2"])
                yB = min(boxA["y2"], boxB["y2"])
                interArea = max(0, xB - xA) * max(0, yB - yA)
                boxAArea = (boxA["x2"] - boxA["x1"]) * (boxA["y2"] - boxA["y1"])
                boxBArea = (boxB["x2"] - boxB["x1"]) * (boxB["y2"] - boxB["y1"])
                unionArea = boxAArea + boxBArea - interArea
                if unionArea == 0:
                    return 0
                return interArea / unionArea

            def center_distance(boxA, boxB):
                cxA = (boxA["x1"] + boxA["x2"]) / 2
                cyA = (boxA["y1"] + boxA["y2"]) / 2
                cxB = (boxB["x1"] + boxB["x2"]) / 2
                cyB = (boxB["y1"] + boxB["y2"]) / 2
                return np.sqrt((cxA - cxB) ** 2 + (cyA - cyB) ** 2)

            for p_idx, person in enumerate(persons):
                for seat in seats:
                    if iou(seat, person) > self.iou_threshold or center_distance(seat, person) < self.proximity_threshold:
                        sitting_person_indices.add(p_idx)
                        break
            persons_sitting = len(sitting_person_indices)

            # Save annotated image
            script_dir = os.path.dirname(os.path.abspath(__file__))
            output_dir = os.path.join(script_dir, '/Users/abdulrahman/Desktop/POC/CameraAi/video-procesing/labeled_images')
            # Use all_detected_objects for annotation if available, otherwise use filtered detected_objects
            objects_to_draw = all_detected_objects if all_detected_objects is not None else detected_objects
            annotated_path = self.save_annotated_image(image, objects_to_draw, image_path, output_dir, camera_id, zone_table_id)

            # Prepare results
            results = {
                 "cameraId": camera_id,
                 "tableId": zone_table_id,
                 "resolution": f"{width}x{height}",
                 "tablesDetected": len(detected_objects["dining table"]),
                 "chairsDetected": len(detected_objects["chair"]),
                 "benchesDetected": len(detected_objects["bench"]),
                 "couchesDetected": len(detected_objects["couch"]),
                 "personsDetected": len(detected_objects["person"]),
                 "totalDetected": sum(len(boxes) for boxes in detected_objects.values()),
                 "occupiedChairs": occupied_chairs,
                 "unoccupiedChairs": unoccupied_chairs,
                 "occupiedBenches": occupied_benches,
                 "unoccupiedBenches": unoccupied_benches,
                 "occupiedCouches": occupied_couches,
                 "unoccupiedCouches": unoccupied_couches,
                 "personsSitting": persons_sitting,
                 "annotatedImagePath": annotated_path,
                 "status": "COMPLETED"
            }

            return results

        except Exception as e:
            # Get table_id from camera zone if available for error reporting
            zone_table_id = None
            if camera_id and camera_id in self.camera_zones:
                zone_table_id = self.camera_zones[camera_id].table_id

            return {
                "status": "error",
                "error_message": str(e),
                "image_path": image_path,
                "camera_id": camera_id,
                "tableId": zone_table_id
            }

def main():
    parser = argparse.ArgumentParser(
        description='Detect objects with camera zone filtering',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Without zone (analyze entire image):
  python analyze_video.py image.jpg --model yolo11x.pt

  # With camera zone (analyze only specific area):
  python analyze_video.py image.jpg --camera-id cam1 --table-id 1 --zone 100 200 400 200 400 500 100 500

  # Multiple cameras (run separately for each):
  python analyze_video.py frame1.jpg --camera-id cam1 --zone 100 150 400 150 400 500 100 500
  python analyze_video.py frame2.jpg --camera-id cam2 --zone 450 150 800 150 800 500 450 500
        """
    )
    parser.add_argument('image_path', help='Path to input image file')
    parser.add_argument('--camera-id', '-cam', default=None, help='Camera identifier (e.g., cam1, cam2)')
    parser.add_argument('--zone', nargs='+', type=int, help='Zone coordinates as: x1 y1 x2 y2 x3 y3 x4 y4 (minimum 6 values for 3 corners)')
    parser.add_argument('--table-id', type=int, default=None, help='Table ID this camera monitors')
    parser.add_argument('--confidence', type=float, default=0.3, help='Confidence threshold for detections (default: 0.3)')
    parser.add_argument('--zone-threshold', type=float, default=0.7, help='Minimum percentage of object in zone to be counted (default: 0.7)')
    parser.add_argument('--show-all', action='store_true', help='Show all detected objects in annotated image (even those outside zone)')
    parser.add_argument('--output-json', action='store_true', help='Output results in JSON format (default: always outputs JSON)')

    args = parser.parse_args()

    try:
        # Initialize detector
        detector = TableChairDetector(confidence_threshold=args.confidence, zone_threshold=args.zone_threshold)

        # Add camera zone if provided
        if args.camera_id and args.zone:
            if len(args.zone) < 6 or len(args.zone) % 2 != 0:
                raise ValueError("Zone must have at least 3 points (6 coordinates)")

            # Convert flat list to list of tuples
            zone_points = [(args.zone[i], args.zone[i+1]) for i in range(0, len(args.zone), 2)]
            detector.add_camera_zone(args.camera_id, zone_points, args.table_id)

        # Process image
        results = detector.process_image_for_service(args.image_path, args.camera_id, args.show_all)
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