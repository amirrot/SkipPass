package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Password {
  public long id;
  public String name;
  public String username;
  public String password;
  public String comment;
  public String group;
  public String additional1;
  public String additional2;
  public String additional3;
  public List<String> past;
  public static final PacketDesc<List<String>> listOfPast = new ListDesc<String>(StrDesc.def);
  public static final PacketDesc<Password> def = new PlainMessageDesc<Password>(new PacketDesc<Password>() {

    @Override
    public Password read(InputStream is) throws IOException, ParseError {
      Password res = new Password();
      res.name = StrDesc.def.read(is);
      res.username = StrDesc.def.read(is);
      res.password = StrDesc.def.read(is);
      res.comment = StrDesc.def.read(is);
      res.group = StrDesc.def.read(is);
      res.additional1 = StrDesc.def.read(is);
      res.additional2 = StrDesc.def.read(is);
      res.additional3 = StrDesc.def.read(is);
      res.past = listOfPast.read(is);
      return res;
    }

    @Override
    public void write(OutputStream os, Password msg) throws IOException {
      StrDesc.def.write(os, msg.name);
      StrDesc.def.write(os, msg.username);
      StrDesc.def.write(os, msg.password);
      StrDesc.def.write(os, msg.comment);
      StrDesc.def.write(os, msg.group);
      StrDesc.def.write(os, msg.additional1);
      StrDesc.def.write(os, msg.additional2);
      StrDesc.def.write(os, msg.additional3);
      listOfPast.write(os, msg.past);
    }

    @Override
    public Password create() {
      Password res = new Password();
      res.name = StrDesc.def.create();
      res.username = StrDesc.def.create();
      res.password = StrDesc.def.create();
      res.comment = StrDesc.def.create();
      res.group = StrDesc.def.create();
      res.additional1 = StrDesc.def.create();
      res.additional2 = StrDesc.def.create();
      res.additional3 = StrDesc.def.create();
      res.past = listOfPast.create();
      return res;
    }
  });

  public boolean match(String selection) {
    if (selection == null || selection.isEmpty()) {
      return true;
    }
    if (name.matches(selection) || username.matches(selection) || password.matches(selection) || group.matches(selection) ||
        comment.matches(selection)) {
      return true;
    }
    return false;
  }
}