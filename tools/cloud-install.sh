#!/usr/bin/env bash
set -euo pipefail
cd /workspace/BetterThanHerobrine
mkdir -p /workspace/tools
# Official archive and checksum are fetched over the existing authenticated proxy.
if [[ ! -x /workspace/tools/jdk-25.0.5.0.3/bin/java ]]; then
  curl --fail --location --retry 2 https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.tar.gz -o /workspace/tools/jdk25.tar.gz
  curl --fail --location --retry 2 https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.tar.gz.sha256 -o /workspace/tools/jdk25.sha256
  python3 - <<'PY'
import hashlib, pathlib, tarfile
p=pathlib.Path('/workspace/tools/jdk25.tar.gz')
expected=pathlib.Path('/workspace/tools/jdk25.sha256').read_text().split()[0]
assert hashlib.file_digest(p.open('rb'),'sha256').hexdigest()==expected, 'JDK checksum mismatch'
with tarfile.open(p) as t: t.extractall('/workspace/tools',filter='data')
PY
fi
# Select an installed Java 25; do not assume the latest URL always returns the same patch.
BTH_JDK=''
for candidate in /workspace/tools/jdk-25*; do
  if [[ -x "$candidate/bin/java" ]]; then BTH_JDK="$candidate"; fi
done
[[ -n "$BTH_JDK" ]]
export JAVA_HOME="$BTH_JDK"
export GRADLE_USER_HOME=/workspace/tools/gradle
# Java does not automatically honor HTTPS_PROXY. Preserve existing options and TLS checks.
if [[ -n "${HTTPS_PROXY:-}" ]]; then
  BTH_PROXY_HOST=$(python3 -c 'import os,urllib.parse; print(urllib.parse.urlsplit(os.environ["HTTPS_PROXY"]).hostname)')
  BTH_PROXY_PORT=$(python3 -c 'import os,urllib.parse; print(urllib.parse.urlsplit(os.environ["HTTPS_PROXY"]).port or 8080)')
  export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Dhttps.proxyHost=$BTH_PROXY_HOST -Dhttps.proxyPort=$BTH_PROXY_PORT -Dhttp.proxyHost=$BTH_PROXY_HOST -Dhttp.proxyPort=$BTH_PROXY_PORT"
fi
if [[ -f /etc/ssl/certs/java/cacerts ]]; then
  export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts"
fi
./gradlew --version
./tools/test-core.sh
python3 tools/check-assets.py
./gradlew build --no-daemon --console=plain
