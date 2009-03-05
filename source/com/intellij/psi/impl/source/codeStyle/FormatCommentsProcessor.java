package com.intellij.psi.impl.source.codeStyle;

import com.intellij.lang.ASTNode;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.java.stubs.JavaStubElementType;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.codeStyle.javadoc.CommentFormatter;
import com.intellij.psi.javadoc.PsiDocComment;

public class FormatCommentsProcessor implements PreFormatProcessor {
  public TextRange process(final ASTNode element, final TextRange range) {
    final Project project = SourceTreeToPsiMap.treeElementToPsi(element).getProject();
    if (!CodeStyleSettingsManager.getSettings(project).ENABLE_JAVADOC_FORMATTING ||
        element.getPsi().getContainingFile().getLanguage() != StdLanguages.JAVA) {
      return range;
    }

    return formatCommentsInner(project, element, range);
  }

  private static TextRange formatCommentsInner(Project project, ASTNode element, final TextRange range) {
    TextRange result = range;


    // check for RepositoryTreeElement is optimization
    if (shouldProcess(element)) {
      final TextRange elementRange = element.getTextRange();

      if (range.contains(elementRange)) {
        new CommentFormatter(project).process(element);
        final TextRange newRange = element.getTextRange();
        result = new TextRange(range.getStartOffset(), range.getEndOffset() + newRange.getLength() - elementRange.getLength());
      }

      // optimization, does not seek PsiDocComment inside fields / methods or out of range
      if (element.getPsi() instanceof PsiField ||
          element.getPsi() instanceof PsiMethod ||
          element instanceof PsiDocComment ||
          range.getEndOffset() < elementRange.getStartOffset()
         ) {
        return result;
      }
    }

    ASTNode current = element.getFirstChildNode();
    while (current != null) {
      // we expand the chameleons here for effectiveness
      current.getFirstChildNode();
      result = formatCommentsInner(project, current, result);
      current = current.getTreeNext();
    }
    return result;
  }

  private static boolean shouldProcess(final ASTNode element) {
    if (element instanceof PsiDocComment) {
      return true;
    }
    else {
      return element.getElementType() instanceof JavaStubElementType &&
          (element.getPsi()) instanceof PsiDocCommentOwner;
    }
  }

}
