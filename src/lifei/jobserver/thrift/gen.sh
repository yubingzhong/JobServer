thrift --gen java -o ../../../.. service.thrift 

cd ../../../../gen-java
cp -r * ..
cd ..
rm -r gen-java
