#pragma version(1)
#pragma rs java_package_name(io.github.cvrunmin.toolbox)

int offsets;

static uchar fourfive(int target) {
    if (target % 16 < 7) {
        target = (int)(target / 255.f * 16.f) * 16 - 1;
    } else {
        target = (int)(target / 255.f * 16.f) * 16 - 1 + 16;
    }
    if (target < 0) {
        target = 0;
    }
    if (target > 255) {
        target = 255;
    }
    return (uchar)target;
}

uchar4 RS_KERNEL groupBits(uchar4 in){
    uchar4 out = in;
    out.r = fourfive(in.r);
    out.g = fourfive(in.g);
    out.b = fourfive(in.b);
    return out;
}