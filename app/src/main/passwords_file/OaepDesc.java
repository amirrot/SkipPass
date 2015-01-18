package com.google.android.skippass.passwords_file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OaepDesc implements PacketDesc<OaepDesc.Oaep> {
  public class Oaep {
    public byte[] seed;
    public byte[] msg;
  }

  private final int m;
  private final int h;
  private final int ds;
  private final int ps;
  private final byte[] zeros;
  public static final OaepDesc def = new OaepDesc(256, 32);

  public OaepDesc(int k, int m) {
    this.m = m;
    this.h = Utils.EMPTY_HASH.length;
    this.ds = k - h - 1;
    this.ps = k - 2 * this.h - m - 2;
    this.zeros = new byte[this.ps];
  }

  private static final byte[] read(InputStream is, int len) throws IOException, ParseError {
    byte[] res = new byte[len];
    if (is.read(res) != len) {
      throw new ParseError();
    }
    return res;
  }

  private static byte[] MGF(byte[] input, int len) {
    byte[] res = new byte[0];
    int i = 0;
    while (res.length < len) {
      byte[] counter = new byte[4];
      counter[0] = (byte) (i >>> 24);
      counter[1] = (byte) (i >>> 16);
      counter[2] = (byte) (i >>> 8);
      counter[3] = (byte) (i >>> 0);
      res = Utils.concat(res, Utils.hash(Utils.concat(input, counter)));
      i++;
    }
    return Utils.cut(res, len);
  }

  @Override public Oaep read(InputStream is) throws IOException, ParseError {
    Oaep res = new Oaep();
    byte[] maskedSeed = read(is, this.h);
    byte[] maskedDb = read(is, this.ds);
    res.seed = Utils.xor(MGF(maskedDb, this.h), maskedSeed);
    byte[] db = Utils.xor(MGF(res.seed, this.ds), maskedDb);
    InputStream new_is = new ByteArrayInputStream(db);
    if (!Utils.compare(read(new_is, this.h), Utils.EMPTY_HASH)) {
      throw new ParseError();
    }
    if (!Utils.compare(read(new_is, this.ps), this.zeros)) {
      throw new ParseError();
    }
    if (read(new_is, 1)[0] != 1) {
      throw new ParseError();
    }
    res.msg = read(new_is, this.m);
    return res;
  }

  @Override public void write(OutputStream os, Oaep msg) throws IOException {
    byte[] seed = msg.seed;
    byte[] message = msg.msg;
    assert seed.length == this.h;
    assert message.length == this.m;
    byte[] db = Utils.concat(Utils.EMPTY_HASH, this.zeros, new byte[] { 1 }, message);
    byte[] maskedDb = Utils.xor(MGF(seed, this.ds), db);
    byte[] maskedSeed = Utils.xor(MGF(maskedDb, this.h), seed);
    os.write(maskedSeed);
    os.write(maskedDb);
  }

  @Override public Oaep create() {
    Oaep res = new Oaep();
    res.seed = new SimpleBufferDesc(this.h).create();
    res.msg = new SimpleBufferDesc(this.m).create();
    return res;
  }
}
