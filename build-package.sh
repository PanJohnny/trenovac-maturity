#!/bin/bash

# 1. Variables based on your POM
NAME="TrenovacMaturity"
VENDOR="PanJohnny"
MAIN_CLASS="me.panjohnny.trenovacmaturity.Launcher"
VERSION="1.0.2"
MAIN_JAR="trenovac-maturity-${VERSION}.jar"
INPUT="target"
DEST="dist"

# 2. Clean up old build
rm -rf "$DEST"
mkdir -p "$DEST"

# 3. Run jpackage
echo "Starting jpackage for $NAME..."

jpackage \
  --type app-image \
  --dest "$DEST" \
  --name "$NAME" \
  --vendor "$VENDOR" \
  --input "$INPUT" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --app-version "$VERSION" \
  --verbose

echo "Build finished. Check the $DEST directory."