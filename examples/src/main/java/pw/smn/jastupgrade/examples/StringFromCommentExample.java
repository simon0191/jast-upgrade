package pw.smn.jastupgrade.examples;

import pw.smn.jastupgrade.annotations.StringFromComment;

public class StringFromCommentExample {

  /**
   *{
   *  "description": "This json comes from a comment",
   *  "hello" : "world",
   *  "blah" : [1, 2, 3],
   *  "four" : {
   *    "x" : "y"
   *  }
   *}
   */
  @StringFromComment
  private static String multiLineJson() {
    return "";
  }

  public static void main(String[] args) {
    System.out.println(multiLineJson());
  }
}