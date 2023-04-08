package mutable_ref

class MutableMfvcWrapper {
    var long0 = 0L
    var long1 = 0L
    var long2 = 0L
    var long3 = 0L
    
    inline fun encodeLong0(value: Double) {
        long0 = value.toRawBits()
    }
    
    inline fun encodeLong1(value: Double) {
        long1 = value.toRawBits()
    }
    
    inline fun encodeLong0(value: Float) {
        long0 = value.toRawBits().toLong()
    }
    
    inline fun encodeLong1(value: Float) {
        long1 = value.toRawBits().toLong()
    }
    
    inline fun encodeLong0(value0: Float, value1: Float) {
        long0 = value0.toRawBits().toLong().shl(32) or value1.toRawBits().toLong().and(0xFFFFFFFF)
    }
    
    inline fun encodeLong0(value0: Int, value1: Int) {
        long0 = value0.toLong().shl(32) or value1.toLong().and(0xFFFFFFFF)
    }
    
    inline fun encodeLong0(value: Int) {
        long0 = value.toLong()
    }
    
    inline fun encodeLong1(value: Int) {
        long1 = value.toLong()
    }
    
    
    inline fun decodeDouble0() = Double.fromBits(long0)
    inline fun decodeDouble1() = Double.fromBits(long1)
    
    inline fun decodeFloat0() = Float.fromBits(long0.toInt())
    inline fun decodeFloat1() = Float.fromBits(long1.toInt())
    
    inline fun decodeInt0() = long0.toInt()
    inline fun decodeInt1() = long1.toInt()
    
    inline fun decodeFloat00() = Float.fromBits(long0.shr(32).toInt())
    inline fun decodeFloat01() = Float.fromBits(long0.toInt())
    
    inline fun decodeInt00() = long0.shr(32).toInt()
    inline fun decodeInt01() = long0.toInt()
}