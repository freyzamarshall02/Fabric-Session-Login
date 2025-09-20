package dev.majanito.screens;

import dev.majanito.SessionIDLoginMod;
import dev.majanito.utils.APIUtils;
import dev.majanito.utils.SessionUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;

import static dev.majanito.utils.FormattingUtils.surroundWithObfuscated;

public class LoginScreen extends Screen {
    private TextFieldWidget sessionField;
    private ButtonWidget loginButton;
    private ButtonWidget restoreButton;

    private Text currentTitle;

    public LoginScreen() {
        super(Text.literal(""));
        this.currentTitle = surroundWithObfuscated(Text.literal("Input Session ID").formatted(Formatting.GOLD), 5);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        sessionField = new TextFieldWidget(
                this.textRenderer,
                centerX - 100,
                centerY,
                200,
                20,
                Text.literal("Session Input")
        );
        sessionField.setMaxLength(32767);
        sessionField.setText("");
        sessionField.setFocused(true);
        this.addSelectableChild(sessionField);

        loginButton = ButtonWidget.builder(Text.literal("Login"), button -> {
            String sessionInput = sessionField.getText().trim();

            if (!sessionInput.isEmpty()) {
                try {
                    String[] sessionInfo = APIUtils.getProfileInfo(sessionInput);

                    SessionUtils.setSession(SessionUtils.createSession(sessionInfo[0], sessionInfo[1], sessionInput));
                    this.currentTitle = surroundWithObfuscated(Text.literal("Logged in as: " + sessionInfo[0]).formatted(Formatting.GREEN), 5);
                    restoreButton.active = true;
                } catch (IOException | RuntimeException e) {
                    this.currentTitle = surroundWithObfuscated(Text.literal("Invalid Session ID").formatted(Formatting.RED), 7);
                }
            } else {
                this.currentTitle = surroundWithObfuscated(Text.literal("Session ID cannot be empty").formatted(Formatting.RED), 5);
            }
        }).dimensions(centerX - 100, centerY + 25, 97, 20).build();
        this.addDrawableChild(loginButton);

        restoreButton = ButtonWidget.builder(Text.literal("Restore"), button -> {
            SessionUtils.restoreSession();
            this.currentTitle = surroundWithObfuscated(Text.literal("Restored original session").formatted(Formatting.GREEN), 7);
            loginButton.active = true;
            restoreButton.active = false;
        }).dimensions(centerX + 3, centerY + 25, 97, 20).build();
        this.addDrawableChild(restoreButton);

        ButtonWidget backButton = ButtonWidget.builder(Text.literal("Back"), button -> {
            assert this.client != null;
            this.client.setScreen(new net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen(new TitleScreen()));
        }).dimensions(centerX - 100, centerY + 50, 200, 20).build();
        this.addDrawableChild(backButton);

        if (SessionIDLoginMod.currentSession.equals(SessionIDLoginMod.originalSession)) {
            restoreButton.active = false;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        sessionField.render(context, mouseX, mouseY, delta);

        // --- FIXED: manual centering + drawTextWithShadow ---
        int textWidth = this.textRenderer.getWidth(this.currentTitle);
        int x = (this.width - textWidth) / 2;
        int y = this.height / 2 - 30;
        context.drawTextWithShadow(this.textRenderer, this.currentTitle, x, y, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (sessionField.keyPressed(keyCode, scanCode, modifiers) || sessionField.isActive()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (sessionField.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}
