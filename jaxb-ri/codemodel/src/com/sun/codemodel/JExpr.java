/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.codemodel;


/**
 * Factory methods that generate various {@link JExpression}s.
 */
public abstract class JExpr {

    /**
     * This class is not instanciable.
     */
    private JExpr() { }

    public static JExpression assign(JAssignmentTarget lhs, JExpression rhs) {
        return new JAssignment(lhs, rhs);
    }

    public static JExpression assignPlus(JAssignmentTarget lhs, JExpression rhs) {
        return new JAssignment(lhs, rhs, "+");
    }

    public static JInvocation _new(JClass c) {
        return new JInvocation(c);
    }

    public static JInvocation _new(JType t) {
        return new JInvocation(t);
    }
    
    public static JInvocation invoke(String method) {
        return new JInvocation((JExpression)null, method);
    }
    
    public static JInvocation invoke(JMethod method) {
        return new JInvocation((JExpression)null,method.name());
    }

    public static JInvocation invoke(JExpression lhs, JMethod method) {
        return new JInvocation(lhs, method.name());
    }

    public static JInvocation invoke(JExpression lhs, String method) {
        return new JInvocation(lhs, method);
    }

    public static JFieldRef ref(String field) {
        return new JFieldRef((JExpression)null, field);
    }

    public static JFieldRef ref(JExpression lhs, JVar field) {
        return new JFieldRef(lhs,field);
    }

    public static JFieldRef ref(JExpression lhs, String field) {
        return new JFieldRef(lhs, field);
    }

    public static JFieldRef refthis(String field) {
         return new JFieldRef(null, field, true);
    }

    public static JExpression dotclass(final JClass cl) {
        return new JExpressionImpl() {
                public void generate(JFormatter f) {
                    JClass c;
                    if(cl instanceof JNarrowedClass)
                        c = ((JNarrowedClass)cl).basis;
                    else
                        c = cl;
                    f.g(c).p(".class");
                }
            };
    }

    public static JArrayCompRef component(JExpression lhs, JExpression index) {
        return new JArrayCompRef(lhs, index);
    }

    public static JCast cast(JType type, JExpression expr) {
        return new JCast(type, expr);
    }

    public static JArray newArray(JType type) {
        return newArray(type,null);
    }

    /**
     * Generates {@code new T[size]}.
     *
     * @param type
     *      The type of the array component. 'T' or {@code new T[size]}.
     */
    public static JArray newArray(JType type, JExpression size) {
        // you cannot create an array whose component type is a generic
        return new JArray(type.erasure(), size);
    }

    /**
     * Generates {@code new T[size]}.
     *
     * @param type
     *      The type of the array component. 'T' or {@code new T[size]}.
     */
    public static JArray newArray(JType type, int size) {
        return newArray(type,lit(size));
    }
    
    
    private static final JExpression __this = new JAtom("this");
    /**
     * Returns a reference to "this", an implicit reference
     * to the current object.
     */
    public static JExpression _this() { return __this; }

    private static final JExpression __super = new JAtom("super");
    /**
     * Returns a reference to "super", an implicit reference
     * to the super class.
     */
    public static JExpression _super() { return __super; }
    
    
    /* -- Literals -- */

    private static final JExpression __null = new JAtom("null");
    public static JExpression _null() {
        return __null;
    }
    
    /**
     * Boolean constant that represents <code>true</code>
     */
    public static final JExpression TRUE = new JAtom("true");
    
    /**
     * Boolean constant that represents <code>false</code>
     */
    public static final JExpression FALSE = new JAtom("false");

    public static JExpression lit(boolean b) {
        return b?TRUE:FALSE;
    }
    
    public static JExpression lit(int n) {
        return new JAtom(Integer.toString(n));
    }

    public static JExpression lit(long n) {
        return new JAtom(Long.toString(n) + "L");
    }

    public static JExpression lit(float f) {
        return new JAtom(Float.toString(f) + "F");
    }

    public static JExpression lit(double d) {
        return new JAtom(Double.toString(d) + "D");
    }

    static final String charEscape = "\b\t\n\f\r\"\'\\";
    static final String charMacro  = "btnfr\"'\\";
    
    /**
     * Escapes the given string, then surrounds it by the specified
     * quotation mark. 
     */
    public static String quotify(char quote, String s) {
        int n = s.length();
        StringBuilder sb = new StringBuilder(n + 2);
        sb.append(quote);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            int j = charEscape.indexOf(c);
            if(j>=0) {
                sb.append('\\');
                sb.append(charMacro.charAt(j));
            } else {
                // technically Unicode escape shouldn't be done here,
                // for it's a lexical level handling.
                // 
                // However, various tools are so broken around this area,
                // so just to be on the safe side, it's better to do
                // the escaping here (regardless of the actual file encoding)
                //
                // see bug 
                if( c<0x20 || 0x7E<c ) {
                    // not printable. use Unicode escape
                    sb.append("\\u");
                    String hex = Integer.toHexString(((int)c)&0xFFFF);
                    for( int k=hex.length(); k<4; k++ )
                        sb.append('0');
                    sb.append(hex);
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append(quote);
        return sb.toString();
    }

    public static JExpression lit(char c) {
        return new JAtom(quotify('\'', "" + c));
    }

    public static JExpression lit(String s) {
        return new JStringLiteral(s);
    }
    
    /**
     * Creates an expression directly from a source code fragment.
     * 
     * <p>
     * This method can be used as a short-cut to create a JExpression.
     * For example, instead of <code>_a.gt(_b)</code>, you can write
     * it as: <code>JExpr.direct("a>b")</code>.
     * 
     * <p>
     * Be warned that there is a danger in using this method,
     * as it obfuscates the object model.
     */
    public static JExpression direct( final String source ) {
        return new JExpressionImpl(){
            public void generate( JFormatter f ) {
                    f.p('(').p(source).p(')');
            }
        };
    }
}
