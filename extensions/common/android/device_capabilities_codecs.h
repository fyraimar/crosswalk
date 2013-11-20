#ifndef XWALK_EXTENSIONS_COMMON_ANDROID_DEVICE_CAPABILITIES_CODECS_H_
#define XWALK_EXTENSIONS_COMMON_ANDROID_DEVICE_CAPABILITIES_CODECS_H_

#include "base/android/jni_helper.h"
#include "base/android/scoped_java_ref.h"


namespace xwalk {
namespace extensions {
class DeviceCapabilitiesCodecs {
 public:
  DeviceCapabilitiesCodecs(JNIEnv* env);
  jint GetString();
}

}
}
