#pragma version(1)
#pragma rs java_package_name(io.github.cvrunmin.toolbox)

int threshold;

uchar4 RS_KERNEL makeThreshold(uchar4 in){
    uchar4 out = in;
    uchar gray = (uchar)(round(in.r * 0.3f + in.g * 0.59f + in.b * 0.11f));
    uchar color = gray >= threshold ? 255 : 0;
    out.r = color;
    out.g = color;
    out.b = color;
    return out;
}