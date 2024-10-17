package com.thesis.sangkapp_ex

object Constants {
    const val DEPTH_MODEL = "fused_model_uint8_512.onnx"
    const val MODEL_PATH = "yolov8n_f32_prototype_final.tflite"
    val LABELS_PATH: String? = null // provide your labels.txt file if the metadata not present in the model
}