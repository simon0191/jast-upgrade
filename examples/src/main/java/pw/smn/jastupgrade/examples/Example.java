package pw.smn.jastupgrade.examples;

import pw.smn.jastupgrade.annotations.StringFromComment;

public class Example {

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

  public static void main(String[] args) throws Exception {
    debug: {
      Thread.sleep(60000);
      System.out.println("Hola");
    }
    debug: { System.out.println("Hola1"); }
    debug: { System.out.println("Hola2"); }
    debug: { System.out.println("Hola3"); }
    notDebug: { System.out.println("Not debug"); }
    
    System.out.println(multiLineJson());
  }
}