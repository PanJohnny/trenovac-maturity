package me.panjohnny.trenovacmaturity.handler;

import me.panjohnny.trenovacmaturity.ExceptionHandler;
import me.panjohnny.trenovacmaturity.MaturitaApplication;
import me.panjohnny.trenovacmaturity.View;
import me.panjohnny.trenovacmaturity.fs.Archiver;
import me.panjohnny.trenovacmaturity.fs.TemporaryFileSystemManager;
import me.panjohnny.trenovacmaturity.pdf.AnswerSetParser;
import me.panjohnny.trenovacmaturity.pdf.ExamPDFParser;

import java.io.File;
import java.io.IOException;

import static me.panjohnny.trenovacmaturity.MaturitaApplication.LOGGER;

@ForViews({View.IN_EXAM, View.ANSWERS_IMPORT, View.ANSWERS_IMPORT_ASSIGN})
public class ExamHandler extends Handler {
    private boolean assigningInProgress;

    public ExamHandler(MaturitaApplication application) {
        super(application);
    }

    public void loadPDF(File file) {
        ExamPDFParser examPDFParser = new ExamPDFParser();
        application.changeView(View.LOADING);
        examPDFParser.parseAsync(file).handleAsync((exam, t) -> {
            if (t != null) {
                ExceptionHandler.handleWarning(t, "Failed to parse an exam PDF");
            }

            this.exam = exam;
            LOGGER.log(System.Logger.Level.INFO, "Imported exam from PDF");

            archivePath = new File(exam.getMeta() + ".maturita");

            application.changeView(View.IN_EXAM);
            return null;
        });
    }

    public void openAnswerSetPDF(File file) {
        try {
            application.changeView(View.LOADING);
            AnswerSetParser answerSetParser = new AnswerSetParser();
            answerSetParser.parseAsync(file).handleAsync((an, t) -> {
                if (t != null) {
                    ExceptionHandler.handleSevere(t, "Failed to parse an answer set PDF");
                }
                this.answers = an;
                assigningInProgress = true;

                LOGGER.log(System.Logger.Level.INFO, "Answers parsed from PDF");
                application.changeView(View.ANSWERS_IMPORT);
                return null;
            });
        } catch (Exception e) {
            ExceptionHandler.handleSevere(e, "Failed to load answer set PDF");
        }
    }

    public void loadQuestionAnswerMap() {
        this.questionAnswerMap = answers.autoAssign(exam);
    }

    public void openExamZIP(File file) {
        application.changeView(View.LOADING);
        try {
            if (!assigningInProgress) {
                TemporaryFileSystemManager.cleanup();
            }
        } catch (IOException e) {
            ExceptionHandler.handleWarning(e, "Failed to clean up temporary file system");
        }

        Archiver.loadArchiveAsync(file.toPath()).handleAsync((maturitaFile, t) -> {
            if (t != null) {
                ExceptionHandler.handleSevere(t, "Failed to load exam from .MATURITA file");
            }
            this.exam = maturitaFile.exam();

            if (!assigningInProgress) {
                this.answers = maturitaFile.answerSet();
                this.questionAnswerMap = maturitaFile.qaMap();
            }

            LOGGER.log(System.Logger.Level.INFO, "Loaded exam from .MATURITA file");
            this.archivePath = file;
            if (assigningInProgress) {
                application.changeView(View.ANSWERS_IMPORT_ASSIGN);
            } else {
                application.changeView(View.IN_EXAM);
            }
            return null;
        });
    }

    @Override
    protected void freeNonDefaults() {
        assigningInProgress = false;
    }

    @Override
    public void save() throws IOException {
        Archiver.createArchive(exam.getMeta(), exam, answers, questionAnswerMap, archivePath.toPath());
    }
}
