# Pass as the first argument build dir, like:
# ./build/linux-ppc64le-normal-server-release
BUILD=$1

JDK=$BUILD/jdk

JAVAC=$JDK/bin/javac
JAVA=$JDK/bin/java

# Please run before 'make build-test-lib' to generate this lib.
# WhiteBox is just used to check if monitor is inflated for
# debugging purposes.
WBJAR=$BUILD/support/test/lib/wb.jar

# Pass hsdis path so it's possible to use -XX:+PrintAssembly and
# -XX:+PrintOptoAssembly
SOURCE=$JDK/../../ # root of source tree
HSDIS=$SOURCE/src/utils/hsdis/build/linux-ppc64le

# Enable verbose
set -x

# javac
$JAVAC \
   -cp $WBJAR \
   --add-exports java.base/jdk.internal.misc=ALL-UNNAMED \
   retry.java

# java
LD_LIBRARY_PATH=$HSDIS \
$JAVA \
   -Xbootclasspath/a:$WBJAR \
   -XX:+UnlockExperimentalVMOptions \
   -XX:+UnlockDiagnosticVMOptions \
   -XX:+WhiteBoxAPI \
   -XX:+UseRTMLocking \
   -XX:+PrintPreciseRTMLockingStatistics \
   -XX:-TieredCompilation \
   -Xcomp \
   -XX:-UseRTMXendForLockBusy \
   -XX:RTMTotalCountIncrRate=1 \
   -XX:RTMRetryCount=$2 \
   -XX:CompileOnly=x.transactionalRegion,x.syncAndTest,x.test \
   --add-exports java.base/jdk.internal.misc=ALL-UNNAMED \
   retry

#  -XX:RTMAbortRatio=10 \
#  -XX:RTMLockingThreshold=100000 \
#  -XX:RTMAbortThreshold=0 \

set +x
