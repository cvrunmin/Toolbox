#pragma version(1)
#pragma rs java_package_name(io.github.cvrunmin.toolbox)

int offsets;

static uint shiftRight(uint src, int dight);

static uint shiftLeft(uint src, int dight){
        if (dight < 0) return shiftRight(src, abs(dight));
        uint temp = src << dight;
        temp = temp | (src >> 24 - dight);
        return temp & 0xFFFFFF;
}

static uint shiftRight(uint src, int dight){
        if (dight < 0) return shiftLeft(src, abs(dight));
        uint temp = src >> dight;
        temp = temp | ((src << 24 - dight) & 0xFFFFFF);
        return temp & 0xFFFFFF;
}

uchar4 RS_KERNEL shiftBits(uchar4 in){
    uchar4 out = in;
    uint rgb = (in.r << 16) + (in.g << 8) + in.b;
    rgb = shiftRight(rgb, offsets);
    out.r = (rgb >> 16) & 255;
    out.g = (rgb >> 8) & 255;
    out.b = rgb & 255;
    return out;
}