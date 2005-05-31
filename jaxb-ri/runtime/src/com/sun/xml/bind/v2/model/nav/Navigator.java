package com.sun.xml.bind.v2.model.nav;

import java.util.Collection;

import com.sun.xml.bind.v2.runtime.Location;

/**
 * Provides unified view of the underlying reflection library,
 * such as {@code java.lang.reflect} and/or APT.
 *
 * <p>
 * This interface provides navigation over the reflection model
 * to decouple the caller from any particular implementation.
 * This allows the JAXB RI to reuse much of the code between
 * the compile time (which works on top of APT) and the run-time
 * (which works on top of {@code java.lang.reflect})
 *
 * <p>
 * {@link Navigator} instances are stateless and immutable.
 *
 *
 * <h2>Parameterization</h2>
 * <h3>ClassDeclT</h3>
 * <p>
 * A Java class declaration (not an interface, a class and an enum.)
 *
 * <h3>TypeT</h3>
 * <p>
 * A Java type. This includs declaration, but also includes such
 * things like arrays, primitive types, parameterized types, and etc.
 *
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public interface Navigator<TypeT,ClassDeclT,FieldT,MethodT> {
    /**
     * Gets the base class of the specified class.
     *
     * @return
     *      null if the parameter represents {@link Object}.
     */
    ClassDeclT getSuperClass(ClassDeclT clazz);

    /**
     * Gets the parameterization of the given base type.
     *
     * <p>
     * For example, given the following
     * <pre><xmp>
     * interface Foo<T> extends List<List<T>> {}
     * interface Bar extends Foo<String> {}
     * </xmp></pre>
     * This method works like this:
     * <pre><xmp>
     * getBaseClass( Bar, List ) = List<List<String>
     * getBaseClass( Bar, Foo  ) = Foo<String>
     * getBaseClass( Foo<? extends Number>, Collection ) = Collection<List<? extends Number>>
     * getBaseClass( ArrayList<? extends BigInteger>, List ) = List<? extends BigInteger>
     * </xmp></pre>
     *
     * @param type
     *      The type that derives from {@code baseType}
     * @param baseType
     *      The class whose parameterization we are interested in.
     * @return
     *      The use of {@code baseType} in {@code type}.
     *      or null if the type is not assignable to the base type.
     */
    TypeT getBaseClass(TypeT type, ClassDeclT baseType);

    /**
     * Gets the fully-qualified name of the class.
     * ("java.lang.Object" for {@link Object})
     */
    String getClassName(ClassDeclT clazz);

    /**
     * Gets the display name of the type object
     *
     * @return
     *      a human-readable name that the type represents.
     */
    String getTypeName(TypeT rawType);

    /**
     * Gets the short name of the class ("Object" for {@link Object})
     */
    String getClassShortName(ClassDeclT clazz);

    /**
     * Gets all the declared fields of the given class.
     */
    Collection<? extends FieldT> getDeclaredFields(ClassDeclT clazz);

    /**
     * Gets all the declared methods of the given class.
     */
    Collection<? extends MethodT> getDeclaredMethods(ClassDeclT clazz);

    /**
     * Gets the class that declares the given field.
     */
    ClassDeclT getDeclaringClassForField(FieldT field);

    /**
     * Gets the class that declares the given method.
     */
    ClassDeclT getDeclaringClassForMethod(MethodT method);

    /**
     * Gets the type of the field.
     */
    TypeT getFieldType(FieldT f);

    /**
     * Gets the name of the field.
     */
    String getFieldName(FieldT field);

    /**
     * Gets the name of the method, such as "toString" or "equals".
     */
    String getMethodName(MethodT m);

    /**
     * Gets the return type of a method.
     */
    TypeT getReturnType(MethodT m);

    /**
     * Returns the list of parameters to the method.
     */
    TypeT[] getMethodParameters(MethodT method);

    /**
     * Returns true if the method is static.
     */
    boolean isStaticMethod(MethodT method);

    /**
     * Checks if {@code sub} is a sub-type of {@code sup}.
     *
     * TODO: should this method take TypeT or ClassDeclT?
     */
    boolean isSubClassOf(TypeT sub, TypeT sup);

    /**
     * Gets the representation of the given Java type in {@code TypeT}.
     *
     * @param c
     *      can be a primitive, array, class, or anything.
     *      (therefore the return type has to be TypeT, not ClassDeclT)
     */
    TypeT ref(Class c);

    /**
     * Gets the TypeT for the given ClassDeclT.
     */
    TypeT use(ClassDeclT c);

    /**
     * If the given type is an use of class declaration,
     * returns the type casted as {@code ClassDeclT}.
     * Otherwise null.
     *
     * <p>
     * TODO: define the exact semantics.
     */
    ClassDeclT asDecl(TypeT type);

    /**
     * Gets the {@code ClassDeclT} representation for the given class.
     *
     * The behavior is undefined if the class object represents
     * primitives, arrays, and other types that are not class declaration.
     */
    ClassDeclT asDecl(Class c);

    /**
     * Checks if the type is an array type.
     */
    boolean isArray(TypeT t);

    /**
     * Checks if the type is an array type but not byte[].
     */
    boolean isArrayButNotByteArray(TypeT t);

    /**
     * Gets the component type of the array.
     *
     * @param t
     *      must be an array.
     */
    TypeT getComponentType(TypeT t);


    /** The singleton instance. */
    public static final ReflectionNavigator REFLECTION = new ReflectionNavigator();

    /**
     * Gets the i-th type argument from a parameterized type.
     *
     * For example, {@code getTypeArgument([Map<Integer,String>],0)=Integer}
     *
     * @throws IllegalArgumentException
     *      If t is not a parameterized type
     * @throws IndexOutOfBoundsException
     *      If i is out of range.
     *
     * @see #isParameterizedType(Object)
     */
    TypeT getTypeArgument(TypeT t, int i);

    /**
     * Returns true if t is a parameterized type.
     */
    boolean isParameterizedType(TypeT t);

    /**
     * Checks if the given type is a primitive type.
     */
    boolean isPrimitive(TypeT t);

    /**
     * Returns the representation for the given primitive type.
     *
     * @param primitiveType
     *      must be Class objects like {@link Integer#TYPE}.
     */
    TypeT getPrimitive(Class primitiveType);

    /**
     * Returns a location of the specified class.
     */
    Location getClassLocation(ClassDeclT clazz);

    Location getFieldLocation(FieldT field);

    Location getMethodLocation(MethodT getter);

    /**
     * Returns true if the given class has a no-arg default constructor.
     * The constructor does not need to be public.
     */
    boolean hasDefaultConstructor(ClassDeclT clazz);

    /**
     * Returns true if the field is static.
     */
    boolean isStaticField(FieldT field);

    /**
     * Returns true if the method is public.
     */
    boolean isPublicMethod(MethodT method);

    /**
     * Returns true if the field is public.
     */
    boolean isPublicField(FieldT field);

    /**
     * Returns true if this is an enum class.
     */
    boolean isEnum(ClassDeclT clazz);

    /**
     * Computes the erasure
     */
    <T> TypeT erasure(TypeT contentInMemoryType);
    // This unused T is necessary to make ReflectionNavigator.erasure work nicely

    /**
     * Returns true if this is an abstract class.
     */
    boolean isAbstract(ClassDeclT clazz);

    /**
     * Gets the enumeration constants from an enum class.
     *
     * @param clazz
     *      must derive from {@link Enum}.
     *
     * @return
     *      can be empty but never null.
     */ 
    FieldT[] getEnumConstants(ClassDeclT clazz);

    /**
     * Gets the representation of the primitive "void" type.
     */
    TypeT getVoidType();
}
