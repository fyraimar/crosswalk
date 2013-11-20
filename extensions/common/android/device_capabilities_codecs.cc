#include "xwalk/extensions/common/android/device_capabilities_codecs.h"

#include "base/android/jni_android.h"
#include "jni/DeviceCapabilitiesCodecs_jni.h"


namespace xwalk {
namespace extensions {
  DeviceCapabilitiesCodecs::DeviceCapabilitiesCodecs(JNIEnv* env) {
  }

  jint GetString(JNIEnv* env, jobject obj){
      return 9999;
  }

  bool RegisterDeviceCapabilitiesCodecs(JNIEnv* env) {
      return RegisterNativesImpl(env) >= 0;
  }
}
}
