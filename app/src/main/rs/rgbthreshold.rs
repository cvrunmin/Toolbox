#pragma version(1)
#pragma rs java_package_name(io.github.cvrunmin.toolbox)

int threshold;

uchar4 RS_KERNEL makeThreshold(uchar4 in){
    uchar4 out = in;
    out.r = in.r >= threshold ? 255 : 0;
    out.g = in.g >= threshold ? 255 : 0;
    out.b = in.b >= threshold ? 255 : 0;
    return out;
}