# Qoi-Java

A Java reimplementation of the **Quite OK Image** (QOI) format. QOI is a fast and simple lossless image compression format that achieves similar sizes to PNG while significantly outperforming it in encoding and decoding speed. It was done as part of one of two projects in the course "Introduction to programming" given by Prof. Jamila Sam. Special thanks to Hamza Remmal for the project idea and structure.  

## Contributors

The code in this repo was co-written by my friend Yassine and me.

## Features
- **Fully implemented in Java** as part of a first-year programming project.
- **Fast and efficient** encoding and decoding.
- **Lossless compression** comparable to PNG.
- **Simple implementation**—easy to read and modify.

## About QOI
QOI was designed to provide a simple and efficient alternative to existing lossless image formats. Compared to PNG:
- **Encoding** is **20x-50x faster**.
- **Decoding** is **3x-4x faster**.
- The file format specification fits on a **single-page PDF**.

For more details on the QOI format, see the [original QOI repository](https://github.com/phoboslab/qoi).

## Usage
### Encoding an Image to Qoi from PNG
```java
pngToQoi(inputFile, outputFile)
```

### Decoding an Image from Qoi to PNG
```java
qoiToPng(inputFile, outputFile)
```



