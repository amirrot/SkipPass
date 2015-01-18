package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

public class LongDesc implements PacketDesc<BigInteger> {
  public static final LongDesc def = new LongDesc();

  private LongDesc() {}

  @Override public BigInteger read(InputStream is) throws IOException, ParseError {
    return buf2Num(BufDesc.def.read(is));
  }

  @Override public void write(OutputStream os, BigInteger msg) throws IOException {
    BufDesc.def.write(os, num2Buf(msg));
  }

  @Override public BigInteger create() {
    return new BigInteger("0");
  }

  public static byte[] num2Buf(BigInteger num) {
    byte[] res = num.toByteArray();
    if (res[0] == 0) {
      return Utils.cut(res, 1, res.length - 1);
    }
    return res;
  }

  public static BigInteger buf2Num(byte[] buf) {
    if (buf[0] < 0) {
      buf = Utils.concat(new byte[1], buf);
    }
    return new BigInteger(buf);
  }
}
