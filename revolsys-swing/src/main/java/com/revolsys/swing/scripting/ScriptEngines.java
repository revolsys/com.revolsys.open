package com.revolsys.swing.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Context.Builder;
import org.graalvm.polyglot.HostAccess;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;

public class ScriptEngines {

  public static final ScriptEngine JS = initGraal();

  public static ScriptEngine initGraal() {
    try {
      final Builder context = Context //
        .newBuilder("js")//
        .allowHostAccess(HostAccess.ALL)//
        .allowHostClassLookup(s -> true)//
      ;
      return GraalJSScriptEngine.create(null, context);
    } catch (final Exception e) {
      return new ScriptEngineManager().getEngineByName("nashorn");
    }
  }
}
