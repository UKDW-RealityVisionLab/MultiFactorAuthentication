import numpy as np
import tensorflow as tf
from tensorflow.lite.python.interpreter import Interpreter
from PIL import Image
import os
from sklearn.metrics import precision_score, recall_score, f1_score, classification_report

# ===== KONFIGURASI =====
MODEL_PATH = "D:/oulunpu/model_anti_spoofing_final_test.tflite"  # atau model_anti_spoofing.tflite
IMAGE_SIZE = 224
THRESHOLD = 0.2  # threshold untuk klasifikasi spoof vs real

REAL_FOLDER = "D:/oulunpu/dataset/test/real"
SPOOF_FOLDER = "D:/oulunpu/dataset/test/spoof"

# ===== MUAT MODEL TFLITE =====
interpreter = Interpreter(model_path=MODEL_PATH)
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

def preprocess_image(image_path):
    image = Image.open(image_path).convert('RGB')
    image = image.resize((IMAGE_SIZE, IMAGE_SIZE))
    image = np.array(image).astype('float32') / 255.0  # normalisasi
    image = np.expand_dims(image, axis=0)  # bentuk (1, 224, 224, 3)
    return image

def predict(image):
    interpreter.set_tensor(input_details[0]['index'], image)
    interpreter.invoke()
    output_data = interpreter.get_tensor(output_details[0]['index'])  # (1, 1)
    return float(output_data[0][0])

def evaluate(folder_path, true_label):
    predictions = []
    for filename in os.listdir(folder_path):
        if not filename.lower().endswith((".png", ".jpg", ".jpeg")):
            continue
        path = os.path.join(folder_path, filename)
        image = preprocess_image(path)
        prob = predict(image)
        #spoof (1) jika >= threshold, real (0) jika < threshold
        pred_label = 0 if prob >= THRESHOLD else 1
        predictions.append((true_label, pred_label))
    return predictions

# ===== EVALUASI SEMUA =====
real_preds = evaluate(REAL_FOLDER, 1)
spoof_preds = evaluate(SPOOF_FOLDER, 0)
all_preds = real_preds + spoof_preds
 
# ===== HITUNG METRIK APCER, BPCER, ACER =====
TP = sum(1 for t, p in all_preds if t == 1 and p == 1)  # Real -> Real
FN = sum(1 for t, p in all_preds if t == 1 and p == 0)  # Real -> Spoof
TN = sum(1 for t, p in all_preds if t == 0 and p == 0)  # Spoof -> Spoof
FP = sum(1 for t, p in all_preds if t == 0 and p == 1)  # Spoof -> Real

APCER = FP / (FP + TN + 1e-6)   # Attack salah diklasifikasikan sebagai real
BPCER = FN / (TP + FN + 1e-6)   # Real salah diklasifikasikan sebagai spoof
ACER = (APCER + BPCER) / 2

# Ambil y_true dan y_pred
y_true = [t for t, _ in all_preds]
y_pred = [p for _, p in all_preds]

# Precision, Recall, F1
precision = precision_score(y_true, y_pred)
recall = recall_score(y_true, y_pred)
f1 = f1_score(y_true, y_pred)

print(f"\n==== METRIK KLASIFIKASI ====")
print(f"Precision : {precision:.4f}")
print(f"Recall    : {recall:.4f}")
print(f"F1-Score  : {f1:.4f}")

# Optional: laporan lengkap per kelas
print("\nClassification Report:")
print(classification_report(y_true, y_pred, target_names=["Spoof (0)", "Real (1)"]))

# ===== CETAK HASIL =====
print(f"\n==== HASIL EVALUASI ====")
print(f"Total real samples: {len(real_preds)}")
print(f"Total spoof samples: {len(spoof_preds)}")
print(f"True Positive (TP): {TP}")
print(f"False Negative (FN): {FN}")
print(f"True Negative (TN): {TN}")
print(f"False Positive (FP): {FP}\n")

print(f"APCER: {APCER:.4f}")
print(f"BPCER: {BPCER:.4f}")
print(f"ACER : {ACER:.4f}")



