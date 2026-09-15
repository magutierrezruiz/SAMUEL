#!/usr/bin/env bash
set -euo pipefail

APP_NAME="Mundo Matemático de Samuel"
PACKAGE_NAME="com.mundomatematico.samuel"
BUILD_TOOLS_VERSION="35.0.0"
API_LEVEL="35"
MIN_API="23"

SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [ -z "$SDK_ROOT" ]; then
  echo "ERROR: ANDROID_SDK_ROOT o ANDROID_HOME no está definido."
  exit 1
fi

BT="$SDK_ROOT/build-tools/$BUILD_TOOLS_VERSION"
ANDROID_JAR="$SDK_ROOT/platforms/android-$API_LEVEL/android.jar"
AAPT2="$BT/aapt2"
D8="$BT/d8"
ZIPALIGN="$BT/zipalign"
APKSIGNER="$BT/apksigner"

for tool in "$AAPT2" "$D8" "$ZIPALIGN" "$APKSIGNER" "$ANDROID_JAR"; do
  if [ ! -e "$tool" ]; then
    echo "ERROR: No se encontró $tool"
    exit 1
  fi
done

ROOT="$(pwd)"
OUT="$ROOT/.build"
RES="$OUT/res"
COMPILED="$OUT/compiled"
GEN="$OUT/gen"
CLASSES="$OUT/classes"
DEX="$OUT/dex"

rm -rf "$OUT" app-debug.apk
mkdir -p "$RES/values" "$COMPILED" "$GEN" "$CLASSES" "$DEX"

cat > "$RES/values/strings.xml" <<XML
<resources>
    <string name="app_name">$APP_NAME</string>
</resources>
XML

echo "== 1/6 Compilar recursos =="
"$AAPT2" compile --dir "$RES" -o "$COMPILED"

echo "== 2/6 Enlazar APK base =="
"$AAPT2" link \
  -o "$OUT/app-unsigned.apk" \
  -I "$ANDROID_JAR" \
  --manifest "$ROOT/AndroidManifest.xml" \
  --java "$GEN" \
  --min-sdk-version "$MIN_API" \
  --target-sdk-version "$API_LEVEL" \
  "$COMPILED"/*.flat

echo "== 3/6 Compilar Java =="
find "$GEN" -name '*.java' -print > "$OUT/sources.txt"
echo "$ROOT/MainActivity.java" >> "$OUT/sources.txt"
javac -encoding UTF-8 -source 8 -target 8 -classpath "$ANDROID_JAR" -d "$CLASSES" @"$OUT/sources.txt"

echo "== 4/6 Crear DEX =="
CLASS_FILES=()
while IFS= read -r -d '' f; do CLASS_FILES+=("$f"); done < <(find "$CLASSES" -name '*.class' -print0)
"$D8" --lib "$ANDROID_JAR" --min-api "$MIN_API" --output "$DEX" "${CLASS_FILES[@]}"
cp "$OUT/app-unsigned.apk" "$OUT/app-with-dex.apk"
(
  cd "$DEX"
  zip -q -j "$OUT/app-with-dex.apk" classes*.dex
)

echo "== 5/6 Alinear =="
"$ZIPALIGN" -f 4 "$OUT/app-with-dex.apk" "$OUT/app-aligned.apk"

echo "== 6/6 Firmar =="
KEYSTORE="$OUT/debug.keystore"
keytool -genkeypair -v \
  -keystore "$KEYSTORE" \
  -storepass android \
  -alias androiddebugkey \
  -keypass android \
  -dname "CN=Android Debug,O=Android,C=US" \
  -keyalg RSA -keysize 2048 -validity 10000 >/dev/null 2>&1

"$APKSIGNER" sign \
  --ks "$KEYSTORE" \
  --ks-key-alias androiddebugkey \
  --ks-pass pass:android \
  --key-pass pass:android \
  --out "$ROOT/app-debug.apk" \
  "$OUT/app-aligned.apk"

"$APKSIGNER" verify --verbose "$ROOT/app-debug.apk"
echo "APK generado: $ROOT/app-debug.apk"
