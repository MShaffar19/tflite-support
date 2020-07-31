/* Copyright 2020 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.lite.task.text.nlclassifier;

import android.content.Context;
import java.nio.ByteBuffer;
import java.util.List;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseTaskApi;
import org.tensorflow.lite.task.core.TaskJniUtils;
import org.tensorflow.lite.task.core.TaskJniUtils.EmptyHandleProvider;

/**
 * Classifier API for NLClassification tasks with Bert models, categorizes string into different
 * classes. The API expects a Bert based TFLite model with metadata populated.
 *
 * <p>The metadata should contain the following information:
 *
 * <ul>
 *   <li>1 input_process_unit for Wordpiece/Sentencepiece Tokenizer.
 *   <li>3 input tensors with names "ids", "mask" and "segment_ids".
 *   <li>1 output tensor of type float32[1, 2], with a optionally attached label file. If a label
 *       file is attached, the file should be a plain text file with one label per line, the number
 *       of labels should match the number of categories the model outputs.
 * </ul>
 */
public class BertNLClassifier extends BaseTaskApi {
  private static final String BERT_NL_CLASSIFIER_NATIVE_LIBNAME = "bert_nl_classifier_jni";

  /**
   * Constructor to initialize the JNI with a pointer from C++.
   *
   * @param nativeHandle a pointer referencing memory allocated in C++.
   */
  private BertNLClassifier(long nativeHandle) {
    super(nativeHandle);
  }

  /**
   * Create {@link BertNLClassifier} from a model file with metadata.
   *
   * @param context Android context
   * @param pathToModel Path to the classification model.
   * @return {@link BertNLClassifier} instance.
   */
  public static BertNLClassifier createBertNLClassifierWithMetadata(
      final Context context, final String pathToModel) {
    return new BertNLClassifier(
        TaskJniUtils.createHandleFromLibrary(
            new EmptyHandleProvider() {
              @Override
              public long createHandle() {
                return initJniWithByteBuffer(TaskJniUtils.loadMappedFile(context, pathToModel));
              }
            },
            BERT_NL_CLASSIFIER_NATIVE_LIBNAME));
  }

  /**
   * Perform classification on a string input, returns classified {@link Category}s.
   *
   * @param text input text to the model.
   * @return A list of Category results.
   */
  public List<Category> classify(String text) {
    return classifyNative(getNativeHandle(), text);
  }

  private static native long initJniWithByteBuffer(ByteBuffer modelBuffer);

  private static native List<Category> classifyNative(long nativeHandle, String text);
}
