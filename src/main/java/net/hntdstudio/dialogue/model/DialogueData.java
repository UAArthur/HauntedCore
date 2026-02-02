package net.hntdstudio.dialogue.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DialogueData {
    private String npcId;
    private String dialogueId;
    private String defaultError;
    private String symbol;
    private List<Dialogue> dialogues;

    @Getter
    @Setter
    public static class Dialogue {
        private int id;
        private List<DialogueCondition> conditions;
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
        private List<DialogueCondition> conditions;
        private List<DialogueAction> actions;
    }

    @Getter
    @Setter
    public static class DialogueCondition {
        private String type; // "quest", "dialogue", "item", "level", "flag", etc.
        private String target; // quest ID, dialogue ID, item ID, etc.
        private String operator; // "completed", "started", "not_started", "has", "lacks", ">=", "==", etc.
        private String value; // For numeric comparisons or specific states
    }

    @Getter
    @Setter
    public static class DialogueAction {
        private String type; // "start_quest", "complete_quest", "give_item", "set_flag", etc.
        private String target;
        private String value;
    }
}