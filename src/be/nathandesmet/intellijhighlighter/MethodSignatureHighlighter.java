package be.nathandesmet.intellijhighlighter;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;

public class MethodSignatureHighlighter implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        if (!(psiElement instanceof PsiMethod)) {
            return;
        }

        PsiMethod method = (PsiMethod) psiElement;
        PsiCodeBlock body = method.getBody();
        if (body == null) {
            return;
        }

        annotatePsiMethod(annotationHolder, method);
    }

    private void annotatePsiMethod(@NotNull AnnotationHolder annotationHolder, PsiMethod method) {
        Document document = getDocument(method);
        if (document == null) {
            return;
        }

        TextRange textRangeOfMethodSignature = getTextRangeOfMethodSignature(method);
        TextRange textRangeForEntireLines = adaptTextRangeToEntireLines(textRangeOfMethodSignature, document);

        Color methodSignatureBg = getBgColor(method);
        TextAttributes attributes = new TextAttributes(JBColor.BLACK, methodSignatureBg, null, EffectType.BOXED, 0);

        annotationHolder.createInfoAnnotation(textRangeForEntireLines, null)
                .setEnforcedTextAttributes(attributes);
    }

    @NotNull
    private Color getBgColor(PsiMethod method) {
        return method.isConstructor()
                ? new Color(238, 255, 170)
                : new Color(234, 234, 234);
    }

    @NotNull
    private TextRange getTextRangeOfMethodSignature(PsiMethod method) {
        int start = Arrays.stream(method.getModifierList().getChildren())
                .map(x -> x.getTextRange().getStartOffset())
                .findFirst()
                .orElse(method.getTextRange().getStartOffset());

        int end = method.getParameterList().getTextRange().getEndOffset();
        return new TextRange(start, end);
    }

    @NotNull
    private TextRange adaptTextRangeToEntireLines(TextRange textRange, Document document) {
        int start = document.getLineStartOffset(document.getLineNumber(textRange.getStartOffset()));
        int end = document.getLineStartOffset(document.getLineNumber(textRange.getEndOffset())+1);
        return new TextRange(start, end);
    }

    @Nullable
    private Document getDocument(PsiElement element) {
        Project project = element.getProject();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        return psiDocumentManager.getDocument(element.getContainingFile());
    }

}
