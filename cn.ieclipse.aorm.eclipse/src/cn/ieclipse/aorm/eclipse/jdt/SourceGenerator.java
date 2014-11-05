/*
 * Copyright 2010 Android ORM projects.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ieclipse.aorm.eclipse.jdt;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Jamling
 * 
 */
public class SourceGenerator {
    public static void main(String[] args) throws Exception {
        // ICompilationUnit unit = JavaCore.createCompilationUnitFrom();
        File f = new File("ExampleContentProvider.java");
        FileReader fr = new FileReader(f);
        char[] b = new char[1024 * 1024];
        int len = fr.read(b);
        char[] c = new char[len];
        System.arraycopy(b, 0, c, 0, len);
        merge(c, "com.ex", "MyContentProvider", "abc", "person.db",
                Arrays.asList(new String[] { "create 1", "create 2" }));
    }

    public static void merge(char[] source, String pkgName, String typeName,
            String auth, String dbName, List<String> tableCreators)
            throws Exception {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source);
        parser.setResolveBindings(true);
        CompilationUnit unit = (CompilationUnit) parser.createAST(null); // parse

        merge(unit, pkgName, typeName, auth, dbName, tableCreators);

        // System.out.println(unit);
    }

    private static void merge(CompilationUnit unit, String pkgName,
            String typeName, String auth, String dbName,
            List<String> tableCreators) {
        unit.recordModifications();
        AST ast = unit.getAST();
        TypeDeclaration type = (TypeDeclaration) unit.types().get(0);
        ImportDeclaration id = ast.newImportDeclaration();
        id.setName(ast.newName("cn.ieclipse.aorm.Session"));
        unit.imports().add(id);

        id = ast.newImportDeclaration();
        id.setName(ast.newName("android.content.UriMatcher"));
        unit.imports().add(id);

        id = ast.newImportDeclaration();
        id.setName(ast.newName("android.database.sqlite.SQLiteDatabase"));
        unit.imports().add(id);

        id = ast.newImportDeclaration();
        id.setName(ast.newName("android.database.sqlite.SQLiteOpenHelper"));
        unit.imports().add(id);

        id = ast.newImportDeclaration();
        id.setName(ast.newName("java.net.Uri"));
        unit.imports().add(id);

        id = ast.newImportDeclaration();
        id.setName(ast.newName("android.content.ContentValue"));
        unit.imports().add(id);

        VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
        vdf.setName(ast.newSimpleName("AUTH"));
        StringLiteral sl = ast.newStringLiteral();
        sl.setLiteralValue(auth);
        vdf.setInitializer(sl);

        FieldDeclaration fd = ast.newFieldDeclaration(vdf);
        fd.modifiers()
                .addAll(ast
                        .newModifiers((Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)));
        fd.setType(ast.newSimpleType(ast.newSimpleName("String")));

        int i = 0;
        type.bodyDeclarations().add(i++, fd);

        // URI = Uri.parse("content://" + AUTH);
        vdf = ast.newVariableDeclarationFragment();
        vdf.setName(ast.newSimpleName("URI"));

        MethodInvocation mi = ast.newMethodInvocation();
        mi.setExpression(ast.newSimpleName("Uri"));
        mi.setName(ast.newSimpleName("parse"));

        InfixExpression fix = ast.newInfixExpression();
        fix.setOperator(InfixExpression.Operator.PLUS);
        sl = ast.newStringLiteral();
        sl.setLiteralValue("content://");
        fix.setLeftOperand(sl);
        fix.setRightOperand(ast.newSimpleName("AUTH"));

        mi.arguments().add(fix);

        vdf.setInitializer(mi);
        fd = ast.newFieldDeclaration(vdf);
        fd.modifiers()
                .addAll(ast
                        .newModifiers((Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)));
        fd.setType(ast.newSimpleType(ast.newSimpleName("Uri")));

        type.bodyDeclarations().add(i++, fd);

        // private mOpenHelper;
        vdf = ast.newVariableDeclarationFragment();
        vdf.setName(ast.newSimpleName("mOpenHelper"));

        fd = ast.newFieldDeclaration(vdf);
        fd.modifiers().add(
                ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        fd.setType(ast.newSimpleType(ast.newName("SQLiteOpenHelper")));
        type.bodyDeclarations().add(i++, fd);

        // private static session;
        vdf = ast.newVariableDeclarationFragment();
        vdf.setName(ast.newSimpleName("session"));

        fd = ast.newFieldDeclaration(vdf);
        fd.modifiers().addAll(
                ast.newModifiers((Modifier.PRIVATE | Modifier.STATIC)));
        fd.setType(ast.newSimpleType(ast.newName("Session")));
        type.bodyDeclarations().add(i++, fd);

        // public static Session getSession(){
        // return session;
        // }

        MethodDeclaration md = ast.newMethodDeclaration();
        md.modifiers().addAll(
                ast.newModifiers((Modifier.PUBLIC | Modifier.STATIC)));
        md.setReturnType2(ast.newSimpleType(ast.newName("Session")));
        md.setName(ast.newSimpleName("getSession"));

        Block methodBlock = ast.newBlock();
        ReturnStatement returnStmt = ast.newReturnStatement();
        returnStmt.setExpression(ast.newSimpleName("session"));
        methodBlock.statements().add(returnStmt);
        md.setBody(methodBlock);
        type.bodyDeclarations().add(i, md);

        // modify onCreate
        rewriteOnCreate(unit, dbName, tableCreators);
    }

    public static CompilationUnit merge(ICompilationUnit source,
            String pkgName, String typeName, String auth, String dbName,
            List<String> tableCreators) throws Exception {

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source);
        parser.setResolveBindings(true);
        CompilationUnit unit = (CompilationUnit) parser.createAST(null); // parse
        merge(unit, pkgName, typeName, auth, dbName, tableCreators);
        return unit;
    }

    private static void rewriteOnCreate(CompilationUnit unit, String dbName,
            List<String> tableCreators) {
        AST ast = unit.getAST();
        TypeDeclaration type = (TypeDeclaration) unit.types().get(0);
        MethodDeclaration onCreate = getMethod(type, ("onCreate"), null);
        if (onCreate != null) {
            Block methodBlock = ast.newBlock();

            // mOpenHelper = new
            // InlineOpenHelper(this.getContext(),"person.db",null,1);
            Assignment a = ast.newAssignment();
            a.setOperator(Assignment.Operator.ASSIGN);

            a.setLeftHandSide(ast.newSimpleName("mOpenHelper"));

            ClassInstanceCreation cc = ast.newClassInstanceCreation();
            cc.setType(ast.newSimpleType(ast.newSimpleName("SQLiteOpenHelper")));
            ThisExpression thisExp = ast.newThisExpression();
            MethodInvocation mi = ast.newMethodInvocation();
            mi.setName(ast.newSimpleName("getContext"));
            mi.setExpression(thisExp);
            cc.arguments().add(mi);
            StringLiteral sl = ast.newStringLiteral();
            sl.setLiteralValue(dbName);

            cc.arguments().add(sl);
            cc.arguments().add(ast.newNullLiteral());
            cc.arguments().add(ast.newNumberLiteral("1"));
            a.setRightHandSide(cc);
            methodBlock.statements().add(ast.newExpressionStatement(a));

            AnonymousClassDeclaration acd = ast.newAnonymousClassDeclaration();
            cc.setAnonymousClassDeclaration(acd);
            genInnerSQLiteOpenHelper(acd, ast, tableCreators);

            a = ast.newAssignment();
            a.setOperator(Assignment.Operator.ASSIGN);

            a.setLeftHandSide(ast.newSimpleName("session"));

            ClassInstanceCreation cic = ast.newClassInstanceCreation();
            cic.setType(ast.newSimpleType(ast.newSimpleName("Session")));

            // SingleVariableDeclaration svd =
            // ast.newSingleVariableDeclaration();
            // svd.setName(ast.newSimpleName("mOpenHelper"));
            cic.arguments().add(ast.newSimpleName("mOpenHelper"));
            // vdf.setInitializer(cic);
            a.setRightHandSide(cic);
            // methodBlock.statements().add(vde);
            methodBlock.statements().add(ast.newExpressionStatement(a));

            ReturnStatement returnStmt = ast.newReturnStatement();
            returnStmt.setExpression(ast.newBooleanLiteral(true));
            methodBlock.statements().add(returnStmt);

            onCreate.setBody(methodBlock);
        }
    }

    private static void genInnerSQLiteOpenHelper(AnonymousClassDeclaration acd,
            AST ast, List<String> tableCreators) {
        MethodDeclaration md = ast.newMethodDeclaration();
        md.modifiers().addAll(ast.newModifiers((Modifier.PUBLIC)));
        md.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
        md.setName(ast.newSimpleName("onCreate"));
        SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
        svd.setName(ast.newSimpleName("db"));
        svd.setType(ast.newSimpleType(ast.newSimpleName("SQLiteDatabase")));
        md.parameters().add(svd);
        Block innerBlock = ast.newBlock();
        md.setBody(innerBlock);
        VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
        vdf.setName(ast.newSimpleName("sql"));
        StringLiteral sl = ast.newStringLiteral();
        sl.setLiteralValue("");
        vdf.setInitializer(sl);
        VariableDeclarationStatement vds = ast
                .newVariableDeclarationStatement(vdf);
        vds.setType(ast.newSimpleType(ast.newSimpleName("String")));
        innerBlock.statements().add(vds);
        for (String creator : tableCreators) {
            String[] lines = creator.split(SourceAnalysis.LF);
            for (String line : lines) {
                Assignment a = ast.newAssignment();
                a.setOperator(Assignment.Operator.PLUS_ASSIGN);
                a.setLeftHandSide(ast.newSimpleName("sql"));
                StringLiteral temp = ast.newStringLiteral();
                temp.setLiteralValue(line);
                a.setRightHandSide(temp);
                innerBlock.statements().add(ast.newExpressionStatement(a));
            }

            MethodInvocation mi = ast.newMethodInvocation();
            mi.setName(ast.newSimpleName("execSQL"));
            mi.setExpression(ast.newSimpleName("db"));
            mi.arguments().add(ast.newSimpleName("sql"));
            innerBlock.statements().add(ast.newExpressionStatement(mi));
        }

        acd.bodyDeclarations().add(md);
        // onUpgrade
        md = ast.newMethodDeclaration();
        md.modifiers().addAll(ast.newModifiers((Modifier.PUBLIC)));
        md.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
        md.setName(ast.newSimpleName("onUpgrade"));
        svd = ast.newSingleVariableDeclaration();
        svd.setName(ast.newSimpleName("db"));
        svd.setType(ast.newSimpleType(ast.newSimpleName("SQLiteDatabase")));
        md.parameters().add(svd);

        svd = ast.newSingleVariableDeclaration();
        svd.setName(ast.newSimpleName("oldVersion"));
        svd.setType(ast.newPrimitiveType(PrimitiveType.INT));
        md.parameters().add(svd);

        svd = ast.newSingleVariableDeclaration();
        svd.setName(ast.newSimpleName("newVersion"));
        svd.setType(ast.newPrimitiveType(PrimitiveType.INT));
        md.parameters().add(svd);

        innerBlock = ast.newBlock();
        md.setBody(innerBlock);
        acd.bodyDeclarations().add(md);
    }

    private static MethodDeclaration getMethod(TypeDeclaration type,
            String name, Type... types) {
        MethodDeclaration result = null;
        MethodDeclaration[] methods = type.getMethods();
        for (MethodDeclaration temp : methods) {
            if (temp.getName().getIdentifier().equals(name)) {
                List params = temp.typeParameters();
                if (params.isEmpty() && types == null) {
                    result = temp;
                    break;
                } else if (params.size() == types.length) {
                    boolean eq = true;
                    for (int i = 0; i < types.length; i++) {
                        if (!params.get(i).equals(types[i])) {
                            eq = false;
                            break;
                        }
                    }
                    if (eq) {
                        result = temp;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static void applyChange(ICompilationUnit cu, CompilationUnit unit) {
        try {
            ASTRewrite rewrite = ASTRewrite.create(unit.getAST());
            ImportRewrite importRewrite = StubUtility.createImportRewrite(unit,
                    false);

            ASTNode node = unit.findDeclaringNode(cu.getTypes()[0].getKey());
            AbstractTypeDeclaration type = ((AbstractTypeDeclaration) node);
            ListRewrite listRewrite = rewrite.getListRewrite(node,
                    type.getBodyDeclarationsProperty());
            MultiTextEdit edit = new MultiTextEdit();
            TextEdit sub = importRewrite.rewriteImports(null);

            edit.addChild(sub);

            // System.out.println(unit);
            org.eclipse.jface.text.Document doc = new org.eclipse.jface.text.Document(
                    cu.getSource());
            TextEdit te = rewrite.rewriteAST(doc, cu.getJavaProject()
                    .getOptions(true));
            te.apply(doc);
            IBuffer buffer = cu.getBuffer();
            buffer.setContents(doc.get());
            buffer.save(null, true);
            // System.out.println(buffer.getContents());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void merge(IType type, String authority, String dbName,
            List<String> tableCreators) throws Exception {
        type.createField("public static final String AUTH=\"" + authority
                + "\";", null, true, null);
        type.createField(
                "public static final Uri URI=Uri.parse(\"content://\" + AUTH);",
                null, true, null);

        type.createField("private SQLiteOpenHelper mOpenHelper;", null, true,
                null);

        type.createField("private static Session session;", null, true, null);

        type.createMethod("public static Session getSession(){"
                + "return session;" + "}", null, true, null);
        IMethod onCreate = type.getMethod("onCreate", null);
        // String doc = onCreate.getAttachedJavadoc(null);
        StringBuilder sb = new StringBuilder(onCreate.getSource());
        int start = sb.indexOf("{") + 1;
        int end = sb.indexOf("}");
        sb.delete(start, end);

        StringBuilder sb2 = new StringBuilder();
        sb2.append("mOpenHelper=new SQLiteOpenHelper(this.getContext(),\"");
        sb2.append(dbName);
        sb2.append("\",null,1){");
        sb2.append("public void onCreate(SQLiteDatabase db){");
        sb2.append(" String sql=\"\";");
        for (String creator : tableCreators) {
            String[] lines = creator.split(SourceAnalysis.LF);
            for (int i = 0; i < lines.length; i++) {
                if (i == 0) {
                    sb2.append("sql=\"");
                } else {
                    sb2.append("sql+=\"");
                }
                sb2.append(lines[i]);
                sb2.append("\";");
            }
            sb2.append("db.execSQL(sql);");
        }
        sb2.append("}");
        sb2.append("public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){");
        sb2.append("");
        sb2.append("}};");
        sb2.append("session = new Session(mOpenHelper, getContext().getContentResolver());");
        sb2.append("return true;");
        sb.insert(start, sb2);

        onCreate.delete(true, null);
        type.createMethod(sb.toString(), null, true, null);

    }
}
