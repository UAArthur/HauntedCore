package net.hntdstudio.dialogue.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DialogueData {
    private String npcId;
    private String dialogueId;
    private List<Dialogue> dialogues;

    @Getter
    @Setter
    public static class Dialogue {
        private int id;
        private String dialogueFrom;
        private String dialogueText;
        private List<DialogueAnswer> dialogueAnswers;
    }

    @Getter
    @Setter
    public static class DialogueAnswer {
        private int id;
        private String dialogueAnswer;
        private int nextDialogue;
    }
}