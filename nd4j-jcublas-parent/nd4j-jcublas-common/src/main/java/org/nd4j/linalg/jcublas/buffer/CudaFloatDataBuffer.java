package org.nd4j.linalg.jcublas.buffer;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.FloatBuffer;
import org.nd4j.linalg.ops.ElementWiseOp;
import org.nd4j.linalg.util.ArrayUtil;

/**
 * Cuda float buffer
 * @author Adam Gibson
 */
public class CudaFloatDataBuffer extends BaseCudaDataBuffer {
    /**
     * Base constructor
     *
     * @param length      the length of the buffer
     */
    public CudaFloatDataBuffer(int length) {
        super(length, Sizeof.FLOAT);
    }

    public CudaFloatDataBuffer(float[] buffer) {
        this(buffer.length);
        setData(buffer);
    }


    @Override
    public void assign(int[] indices, float[] data, boolean contiguous,int inc) {
        if(indices.length != data.length)
            throw new IllegalArgumentException("Indices and data length must be the same");
        if(indices.length > length())
            throw new IllegalArgumentException("More elements than space to assign. This buffer is of length " + length() + " where the indices are of length " + data.length);

        if(contiguous) {
            int offset = indices[0];
            Pointer p = Pointer.to(data);
            set(offset,data.length,p,inc);
        }
        else
            throw new UnsupportedOperationException("Only contiguous supported");
    }

    @Override
    public void assign(int[] indices, double[] data, boolean contiguous,int inc) {
        if(indices.length != data.length)
            throw new IllegalArgumentException("Indices and data length must be the same");
        if(indices.length > length())
            throw new IllegalArgumentException("More elements than space to assign. This buffer is of length " + length() + " where the indices are of length " + data.length);

        if(contiguous) {
            int offset = indices[0];
            Pointer p = Pointer.to(data);
            set(offset,data.length,p,inc);
        }
        else
            throw new UnsupportedOperationException("Only contiguous supported");
    }





    @Override
    public double[] getDoublesAt(int offset, int length) {
        return ArrayUtil.toDoubles(getFloatsAt(offset, length));
    }

    @Override
    public float[] getFloatsAt(int offset, int length) {
        if(offset + length > length())
            length -= offset;
        float[] ret = new float[length];
        Pointer p = Pointer.to(ret);
        get(offset,length,p);
        return ret;
    }

    @Override
    public void assign(Number value, int offset) {
        int arrLength = length - offset;
        float[] data = new float[arrLength];
        for(int i = 0; i < data.length; i++)
            data[i] = value.floatValue();
        set(offset,arrLength,Pointer.to(data));

    }

    @Override
    public void setData(int[] data) {
        setData(ArrayUtil.toFloats(data));
    }

    @Override
    public void setData(float[] data) {

        if(data.length != length)
            throw new IllegalArgumentException("Unable to set vector, must be of length " + length() + " but found length " + data.length);

        if(pointer() == null)
            alloc();

        JCublas.cublasSetVector(
                length,
                elementSize,
                Pointer.to(data),
                1,
                pointer(),
                1);
    }

    @Override
    public void setData(double[] data) {
        setData(ArrayUtil.toFloats(data));
    }

    @Override
    public byte[] asBytes() {
        return new byte[0];
    }

    @Override
    public int dataType() {
        return DataBuffer.FLOAT;
    }

    @Override
    public float[] asFloat() {
        float[] ret = new float[length];
        Pointer p = Pointer.to(ret);
        JCublas.cublasGetVector(
                length,
                elementSize(),
                pointer(),
                1,
                p,
                1);
        return ret;
    }

    @Override
    public double[] asDouble() {
        return ArrayUtil.toDoubles(asFloat());
    }

    @Override
    public int[] asInt() {
        return ArrayUtil.toInts(asFloat());
    }


    @Override
    public double getDouble(int i) {
        return getFloat(i);
    }

    @Override
    public float getFloat(int i) {
        float[] data = new float[1];
        Pointer p = Pointer.to(data);
        get(i,p);
        return data[0];
    }

    @Override
    public Number getNumber(int i) {
        return getFloat(i);
    }



    @Override
    public void put(int i, float element) {
        float[] data = new float[]{element};
        Pointer p = Pointer.to(data);
        set(i,p);
    }

    @Override
    public void put(int i, double element) {
        put(i,(float) element);
    }

    @Override
    public void put(int i, int element) {
        put(i,(float) element);
    }




    @Override
    public int getInt(int ix) {
        return (int) getFloat(ix);
    }

    @Override
    public DataBuffer dup() {
        CudaFloatDataBuffer buf = new CudaFloatDataBuffer(length());
        copyTo(buf);
        return buf;
    }

    @Override
    public void flush() {

    }

    @Override
    public void apply(ElementWiseOp op, int offset) {
        if(offset >= length)
            throw new IllegalArgumentException("Illegal start " + offset + " greater than length of " + length);
        int arrLength = Math.abs(length - offset);
        float[] data = new float[arrLength];
        Pointer p = Pointer.to(data);
        get(offset,length(),p);
        DataBuffer floatBuffer = new FloatBuffer(data,false);
        floatBuffer.apply(op);
        p = Pointer.to(data);
        set(offset,arrLength,p);

    }


}
