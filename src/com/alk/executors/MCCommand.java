package com.alk.executors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MCCommand {
	/// This is required, the cmd and all its aliases
    String[] cmds() default {};

    /// Verify the number of parameters, inGuild and notInGuild imply min if they have an index > number of args
    int min() default 0;
    int max() default Integer.MAX_VALUE;
    
    int order() default -1;
    boolean op() default false;
    
    boolean inGame() default false;
    int[] online() default {}; /// Implies inGame = true
    int[] ints() default {};
    
    int[] ports() default {};
    int[] playerQuery() default {};
    
    String usage() default "";
    String usageNode() default "";
	String perm() default "";

	int[] alphanum() default {};
    
}