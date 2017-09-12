import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by f on 2/10/2017.
 */
public class ListAndInsert extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {

        final Project project = e.getProject();
        Editor editor = FileEditorManager.getInstance(project)
                                         .getSelectedTextEditor();
        CaretModel caret = editor.getCaretModel();
        Document document = editor.getDocument();
        int pos;
        if ((pos = inString(caret, document)) <= 0) return;

        TextRange textRange = new TextRange(pos, caret.getOffset());
        String dirName = document.getText(textRange);
//            System.out.println(dirName);
        try {
            if (!dirName.contains(File.separator)) return;

            List<String> list = getStrings(dirName);
            //if (list.size() == 0) return;

            ListPopup listPopup =
                    JBPopupFactory.getInstance()
                                  .createListPopup(
                                          createtListPopupStep(project, editor, dirName, list),
                                          10);

            listPopup.showInBestPositionFor(e.getDataContext());

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @NotNull
    private BaseListPopupStep<String> createtListPopupStep(Project project, Editor editor, String dirName, List<String> list) {
        return new BaseListPopupStep<String>("DON'T PANIC!!!", list) {
            @Override
            public PopupStep onChosen(String selectedValue, boolean finalChoice) {

                String dir = selectedValue
                        .replace("\\", "\\\\")
                        .replace(dirName, "");

                Runnable r = () ->
                        EditorModificationUtil
                                .insertStringAtCaret(editor, dir);
                WriteCommandAction.runWriteCommandAction(project, r);

                return PopupStep.FINAL_CHOICE;
            }
        };
    }

    private List<String> getStrings(String dirName) {
        try {
            if (!Files.isDirectory(Paths.get(dirName)) && !Files.isRegularFile(Paths.get(dirName))) {

                Files.list(Paths.get(Paths.get(dirName)
                                          .getParent()
                                          .toString()
                                          .replace("\\\\", "\\")))
                     .map(Path::toString)
                     .collect(Collectors.toList());
            }
            return Files.list(Paths.get(dirName.replace("\\\\", "\\")))
                        .map(Path::toString)
                        .collect(Collectors.toList());

        } catch (Exception e) {
//            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private int inString(CaretModel caret, Document document) {
        int lineStart = caret.getVisualLineStart();
        int lineEnd = caret.getVisualLineEnd();
        String lineText = document.getText(new TextRange(lineStart, lineEnd));
        if (!lineText.contains("\"")) {
            return -1;
        }
        int current = caret.getLogicalPosition().column;
        if (lineText.charAt(current) == '\\') return -1;
        int start = -1;
        for (int i = current - 1; i >= 0; i--) {
            if (lineText.charAt(i) == '"') {
                start = i;
                break;
            }
        }
        int end = lineText.indexOf('"', start + 1);
        if (current >= start && current <= end) {
            return lineStart + start + 1;
        }


        return -1;
    }
}
