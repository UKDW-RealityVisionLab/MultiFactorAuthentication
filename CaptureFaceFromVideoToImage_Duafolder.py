import uuid
import argparse
import cv2
import os
import numpy as np
from pathlib import Path

# Argument parser
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--input", type=str, required=True, help="Path to input video folder")
ap.add_argument("-o", "--output", type=str, required=True, help="Path to output directory")
ap.add_argument("-width", "--width", type=int, required=True, help="Image resize width")
ap.add_argument("-height", "--height", type=int, required=True, help="Image resize height")
ap.add_argument("-s", "--skip", type=int, default=20, help="Frames to skip before processing")
ap.add_argument("-f", "--flip", type=int, default=0, help="Flip cropped faces (0 = no, 1 = yes)")
args = vars(ap.parse_args())

# Set dimensions
dim = (args["width"], args["height"])

# Load face detector
print("[INFO] Loading face detector...")
face_detector = "face_detector/"
protoPath = os.path.join(face_detector, "deploy.prototxt")
modelPath = os.path.join(face_detector, "res10_300x300_ssd_iter_140000.caffemodel")
net = cv2.dnn.readNetFromCaffe(protoPath, modelPath)
confidenceThreshold = 0.7

# Output directories
real_dir = os.path.join(args["output"], "real")
spoof_dir = os.path.join(args["output"], "spoof")
os.makedirs(real_dir, exist_ok=True)
os.makedirs(spoof_dir, exist_ok=True)

# Process videos
video_extensions = (".avi", ".mp4", ".mov", ".mkv", ".flv", ".wmv")
for file in os.listdir(args["input"]):
    if file.endswith(video_extensions):
        path = os.path.join(args["input"], file)
        filename = Path(file).stem
        parts = filename.split("_")
        if len(parts) < 4:
            print(f"[WARNING] Skipping {file}, incorrect filename format")
            continue
        
        file_num = int(parts[3])
        if file_num == 1:
            category = "real"
            save_dir = real_dir
        else:
            category = "spoof"
            save_dir = spoof_dir   
        
        print(f"[INFO] Processing {file} as {category}")
        vs = cv2.VideoCapture(path)
        read = 0
        saved = 0
        
        while True:
            if saved >= 5:
                break
            
            grabbed, frame = vs.read()
            if not grabbed:
                break
            
            read += 1
            if read % args["skip"] != 0:
                continue
            
            h, w = frame.shape[:2]
            blob = cv2.dnn.blobFromImage(cv2.resize(frame, (224, 224)), 1.0, (224, 224), (104.0, 177.0, 123.0))
            net.setInput(blob)
            detections = net.forward()
            
            if len(detections) > 0:
                i = np.argmax(detections[0, 0, :, 2])
                confidence = detections[0, 0, i, 2]
                
                if confidence > confidenceThreshold:
                    box = detections[0, 0, i, 3:7] * np.array([w, h, w, h])
                    (startX, startY, endX, endY) = box.astype("int")
                    boxWidth, boxHeight = endX - startX, endY - startY
                    diff = boxHeight - boxWidth
                    newStartX, newEndX = int(startX - diff / 2), int(endX + diff / 2)
                    face = frame[startY:endY, newStartX:newEndX]
                    
                    if face.size != 0:
                        resized = cv2.resize(face, dim, interpolation=cv2.INTER_AREA)
                        if args["flip"]:
                            resized = cv2.flip(resized, 0)
                        
                        save_filename = f"{filename}_{saved+1}.png"
                        save_path = os.path.join(save_dir, save_filename)
                        cv2.imwrite(save_path, resized)
                        saved += 1
                        print(f"[INFO] Saved {save_path}")
        
        vs.release()

cv2.destroyAllWindows()
print("[INFO] Processing complete.")
