package org.codehaus.mojo.unix.deb;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import fj.*;
import fj.Function;
import static fj.Function.*;
import fj.data.*;
import static fj.data.List.*;
import static fj.data.List.join;
import static fj.data.Option.*;
import static fj.pre.Ord.*;
import org.codehaus.mojo.unix.java.*;
import static org.codehaus.mojo.unix.java.StringF.*;

/**
 * @author <a href="mailto:trygvis@codehaus.org">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ControlFile
{
    /**
     * Refers to the <code>Package</code> field, but "package" is a reserved keyword in Java.
     */
    public final String packageName;

    public final Option<String> version;

    public final Option<String> description;

    public final Option<String> maintainer;

    public final Option<String> architecture;

    public final Option<String> priority;

    public final Option<String> section;

    public final List<String> depends;

    public final List<String> recommends;

    public final List<String> suggests;

    public final List<String> preDepends;

    public final List<String> provides;

    public final List<String> replaces;

    public final List<String> conflicts;

    public final TreeMap<String, String> extraFields;

    public ControlFile( String packageName )
    {
        this( packageName, Option.<String>none(), Option.<String>none(), Option.<String>none(), Option.<String>none(),
              Option.<String>none(), Option.<String>none(), List.<String>nil(), List.<String>nil(), List.<String>nil(),
              List.<String>nil(), List.<String>nil(), List.<String>nil(), List.<String>nil(),
              TreeMap.<String, String>empty( stringOrd ) );
    }

    public ControlFile( String packageName, Option<String> version, Option<String> description,
                        Option<String> maintainer, Option<String> architecture, Option<String> priority,
                        Option<String> section, List<String> depends, List<String> recommends, List<String> suggests,
                        List<String> preDepends, List<String> provides, List<String> replaces, List<String> conflicts,
                        TreeMap<String, String> extraFields )
    {
        this.packageName = packageName;
        this.version = version;
        this.description = description;
        this.maintainer = maintainer;
        this.architecture = architecture;
        this.priority = priority;
        this.section = section;
        this.depends = depends;
        this.recommends = recommends;
        this.suggests = suggests;
        this.preDepends = preDepends;
        this.provides = provides;
        this.replaces = replaces;
        this.conflicts = conflicts;
        this.extraFields = extraFields;
    }

    public ControlFile version( Option<String> version )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile description( Option<String> description )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile maintainer( Option<String> maintainer )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile architecture( Option<String> architecture )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile priority( Option<String> priority )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile section( Option<String> section )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile depends( List<String> depends )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile recommends( List<String> recommends )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile suggests( List<String> suggests )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile preDepends( List<String> preDepends )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile provides( List<String> provides )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile replaces( List<String> replaces )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile conflicts( List<String> conflicts )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile extraFields( TreeMap<String, String> extraFields )
    {
        return new ControlFile( packageName, version, description, maintainer, architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public List<String> toList()
    {
        List<Option<String>> optionList = single( some( "Package: " + packageName ) ).
            cons( section.map( curry( concat, "Section: " ) ) ).
            cons( priority.map( curry( concat, "Priority: " ) ) ).
            cons( maintainer.map( curry( concat, "Maintainer: " ) ) ).
            cons( version.map( curry( concat, "Version: " ) ) ).
            cons( architecture.map( curry( concat, "Architecture: " ) ) ).
            cons( description.map( curry( concat, "Description: " ) ) );

        F<List<String>, Boolean> isNotEmpty = isNotEmpty_();

        List<Option<List<String>>> lists = List.<Option<List<String>>>nil().
            cons( iif( isNotEmpty, this.depends ).map( transformList.f( "Depends" ) ) ).
            cons( iif( isNotEmpty, this.recommends ).map( transformList.f( "Recommends: " ) ) ).
            cons( iif( isNotEmpty, this.suggests ).map( transformList.f( "Suggests: " ) ) ).
            cons( iif( isNotEmpty, this.preDepends ).map( transformList.f( "Pre-Depends: " ) ) ).
            cons( iif( isNotEmpty, this.provides ).map( transformList.f( "Provides: " ) ) ).
            cons( iif( isNotEmpty, this.replaces ).map( transformList.f( "Replaces: " ) ) ).
            cons( iif( isNotEmpty, this.conflicts ).map( transformList.f( "Conflicts: " ) ) );

        return somes( optionList ).reverse().append( join( somes( lists ) ) );
    }

    F2<String, String, String> folder = new F2<String, String, String>()
    {
        public String f( String a, String b )
        {
            return a + ", " + b;
        }
    };

    F<String, F<List<String>, List<String>>> transformList = new F<String, F<List<String>, List<String>>>()
    {
        F<List<String>, String> foldLeft = List.<String, String>foldLeft().f( curry( folder ) ).f( "" );

        public F<List<String>, List<String>> f( final String fieldName )
        {
            return new F<List<String>, List<String>>()
            {
                public List<String> f( List<String> stringList )
                {
                    String head = fieldName + stringList.head();

                    return single( head ).append( stringList.tail() );
                }
            };
        }
    };

//    public final F<String, List<String>> formatField = new F<String, List<String>>()
//    {
//        public List<String> f( String value )
//        {
//            return formatField( value );
//        }
//    };
//
//    public List<String> formatField( String value )
//    {
//        System.out.println( "value = " + value );
//        System.out.println( "value.length() = " + value.length() );
//
//        return List.unfold( new F<P2<String, Boolean>, Option<P2<String, P2<String, Boolean>>>>()
//        {
//            public Option<P2<String, P2<String, Boolean>>> f( P2<String, Boolean> state )
//            {
//                String value = state._1();
//
//                if ( value.length() == 0 )
//                {
//                    return none();
//                }
//
//                int cut = value.indexOf( System.getProperty( "line.separator" ) );
//
//                // If a EOL is found before 77 chars, return it
//                if ( cut < 77 )
//                {
//                    return some( p( value.substring( 0, cut ), p( value.substring( cut ), false ) ) );
//                }
//
//                cut = value.lastIndexOf( ' ', cut );
//
//                String v = state._2() ? "" : " " + value.substring( 0, cut - 1 ).trim();
//
//                return some( p( v, p( value.substring( cut ), false ) ) );
//            }
//        }, P.<String, Boolean>p( value, true ) );
//    }

//    public static final F<String, List<String>> toList = new F<String, List<String>>()
//    {
//        public List<String> f( String value )
//        {
//            return List.list( value.split( "," ) );
//        }
//    };

    public static final F<String, List<String>> toList = compose( List.<String, String>map_().f( StringF.trim ),
                                                                  Function.flip( StringF.split ).f( "," ) );

//    public void streamTo( LineStreamWriter control )
//    {
//        F2<String, String, String> folder = new F2<String, String, String>()
//        {
//            public String f( String a, String b )
//            {
//                return a + ", " + b;
//            }
//        };
//
//        control.
//            add( "Section: " + UnixUtil.getField( "section", section ) ).
//            add( "Priority: " + priority.orSome( "standard" ) ).
//            add( "Maintainer: " + UnixUtil.getField( "maintainer", maintainer ) ).
//            add( "Package: " + packageName ).
//            add( "Version: " + version ).
//            add( "Architecture: " + UnixUtil.getField( "architecture", architecture ) ).
//            addIf( depends.isNotEmpty(), "Depends: " + depends.foldRight( folder,  "" ) ).
//            addIf( recommends.isNotEmpty(), "Recommends: " + recommends.foldRight( folder,  "" ) ).
//            addIf( suggests.isNotEmpty(), "Suggests: " + suggests.foldRight( folder,  "" ) ).
//            addIf( preDepends.isNotEmpty(), "Pre-Depends: " + preDepends.foldRight( folder,  "" ) ).
//            addIf( provides.isNotEmpty(), "Provides: " + provides.foldRight( folder,  "" ) ).
//            addIf( replaces.isNotEmpty(), "Replaces: " + replaces.foldRight( folder,  "" ) ).
//            add( "Description: " + description );
//    }

    public static ControlFile controlFileFromList( List<P2<String, String>> values )
    {
        TreeMap<String, String> map =
            values.foldLeft( new F2<TreeMap<String, String>, P2<String, String>, TreeMap<String, String>>()
            {
                public TreeMap<String, String> f( TreeMap<String, String> map, P2<String, String> p )
                {
                    return map.set( p._1(), p._2() );
                }
            }, TreeMap.<String, String>empty( stringOrd ) );

        Option<String> packageName = map.get( "Package" );

        if ( packageName.isNone() )
        {
            throw Bottom.error( "Could not find required field 'Package'." );
        }

        ControlFile controlFile = new ControlFile( packageName.some() );

        controlFile = controlFile.version( map.get( "Version" ) );
        map = map.delete( "Version" );

        controlFile = controlFile.description( map.get( "Description" ) );
        map = map.delete( "Description" );

        controlFile = controlFile.maintainer( map.get( "Maintainer" ) );
        map = map.delete( "Maintainer" );

        controlFile = controlFile.architecture( map.get( "Architecture" ) );
        map = map.delete( "Architecture" );

        controlFile = controlFile.priority( map.get( "Priority" ) );
        map = map.delete( "Priority" );

        controlFile = controlFile.section( map.get( "Section" ) );
        map = map.delete( "Section" );

        List<String> emptyList = List.nil();
        controlFile = controlFile.depends( map.get( "Depends" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Depends" );

        controlFile = controlFile.recommends( map.get( "Recommends" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Recommends" );

        controlFile = controlFile.suggests( map.get( "Suggests" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Suggests" );

        controlFile = controlFile.preDepends( map.get( "PreDepends" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "PreDepends" );

        controlFile = controlFile.provides( map.get( "Provides" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Provides" );

        controlFile = controlFile.replaces( map.get( "replaces" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Replaces" );

        controlFile = controlFile.conflicts( map.get( "conflicts" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Conflicts" );

        controlFile.extraFields( map );

        return controlFile;
    }
}
