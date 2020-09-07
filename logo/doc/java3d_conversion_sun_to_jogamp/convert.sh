#! /bin/sh

grep -RIl javax.media.j3d src > files.javax.media.j3d.txt
for i in `cat files.javax.media.j3d.txt` ; do sed -i 's/javax.media.j3d/org.jogamp.java3d/g' $i ; done

grep -RIl javax.vecmath src > files.javax.vecmath.txt
for i in `cat files.javax.vecmath.txt` ; do sed -i 's/javax.vecmath/org.jogamp.vecmath/g' $i ; done

grep -RIl com.sun.j3d src > files.com.sun.j3d.txt
for i in `cat files.com.sun.j3d.txt` ; do sed -i 's/com.sun.j3d/org.jogamp.java3d/g' $i ; done

