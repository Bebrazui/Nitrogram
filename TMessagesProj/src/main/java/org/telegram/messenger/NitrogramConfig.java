package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.graphics.ColorUtils;

import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.tl.TL_stars;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.LauncherIconController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public final class NitrogramConfig {

    private static final String PREFS_NAME = "nitrogram_config";
    private static final String KEY_HIDE_TYPING = "hide_typing";
    private static final String KEY_GHOST_READ = "ghost_read";
    private static final String KEY_UNLIMITED_PINS = "unlimited_pins";
    private static final String KEY_UNLIMITED_FOLDERS = "unlimited_folders";
    private static final String KEY_UNLIMITED_FOLDER_CHATS = "unlimited_folder_chats";
    private static final String KEY_SORT_MODE = "sort_mode";
    private static final String KEY_SORT_UNREAD_FIRST = "sort_unread_first";
    private static final String KEY_SORT_FOLDERS_FIRST = "sort_folders_first";
    private static final String KEY_SORT_CHANNELS_FIRST = "sort_channels_first";
    private static final String KEY_SORT_CONTACTS_FIRST = "sort_contacts_first";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_APP_ICON_MODE = "app_icon_mode";
    private static final String KEY_BUBBLE_RADIUS = "bubble_radius";
    private static final String KEY_CHAT_DENSITY = "chat_density";
    private static final String KEY_SWIPE_ACTION = "swipe_action";
    private static final String KEY_HIDE_STORIES = "hide_stories";
    private static final String KEY_HIDE_REACTIONS = "hide_reactions";
    private static final String KEY_DEV_HIDE_DIALOG_AVATARS = "dev_hide_dialog_avatars";
    private static final String KEY_DEV_HIDE_STATUS_ICONS = "dev_hide_status_icons";
    private static final String KEY_DEV_HIDE_PINNED_MARKERS = "dev_hide_pinned_markers";
    private static final String KEY_DEV_DIALOG_AVATAR_RADIUS = "dev_dialog_avatar_radius";
    private static final String KEY_DEV_DIALOG_PADDING = "dev_dialog_padding";
    private static final String KEY_DEV_DIALOG_TEXT_SPACING = "dev_dialog_text_spacing";
    private static final String KEY_DEV_UNREAD_BADGE_SCALE = "dev_unread_badge_scale";
    private static final String KEY_DEV_BUBBLE_TOP_RADIUS = "dev_bubble_top_radius";
    private static final String KEY_DEV_BUBBLE_BOTTOM_RADIUS = "dev_bubble_bottom_radius";
    private static final String KEY_DEV_OUTGOING_BUBBLE_COLOR = "dev_outgoing_bubble_color";
    private static final String KEY_DEV_INCOMING_BUBBLE_COLOR = "dev_incoming_bubble_color";
    private static final String KEY_DEV_ACTION_BAR_COLOR = "dev_action_bar_color";
    private static final String KEY_DEV_CHAT_LIST_BACKGROUND_COLOR = "dev_chat_list_background_color";
    private static final String KEY_PREMIUM_VISUAL_MODE = "premium_visual_mode";
    private static final String KEY_SHOW_PREMIUM_BADGES = "show_premium_badges";
    private static final String KEY_SHOW_PREMIUM_GRADIENT = "show_premium_gradient";
    private static final String KEY_SHOW_PROFILE_GLOW = "show_profile_glow";
    private static final String KEY_SHOW_ANIMATED_ICONS = "show_animated_icons";
    private static final String KEY_PREMIUM_TAB_STYLE = "premium_tab_style";

    private static final String KEY_FAKE_IDENTITY_ENABLED = "fake_identity_enabled";
    private static final String KEY_FAKE_PHONE = "fake_phone";
    private static final String KEY_FAKE_USERNAME = "fake_username";
    private static final String KEY_FAKE_USERNAMES_EXTRA = "fake_usernames_extra";
    private static final String KEY_FAKE_FIRST_NAME = "fake_first_name";
    private static final String KEY_FAKE_LAST_NAME = "fake_last_name";

    private static final String KEY_USE_MATERIAL3_COMPONENTS = "use_material3_components";
    public static final int MAX_LOCAL_LIMIT = 9999;
    public static final int MAX_ACCOUNT_SLOTS = 16;
    public static final int SORT_BY_ACTIVITY = 0;
    public static final int SORT_BY_UNREAD = 1;
    public static final int SORT_BY_TYPE = 2;
    public static final int THEME_SYSTEM = 0;
    public static final int THEME_NITRO = 1;
    public static final int THEME_MIDNIGHT = 2;
    private static final int BLUE_DEFAULT_ACCENT_ID = Theme.DEFALT_THEME_ACCENT_ID;
    private static final int DARK_BLUE_DEFAULT_ACCENT_ID = 0;
    private static final int NITRO_BLUE_ACCENT_ID = 11;
    private static final int NITRO_DARK_BLUE_ACCENT_ID = 11;
    private static final int MIDNIGHT_ACCENT_ID = 5;
    public static final int ICON_DEFAULT = 0;
    public static final int ICON_FILLED = 1;
    public static final int ICON_MINIMAL = 2;
    public static final int MIN_CHAT_DENSITY = 1;
    public static final int MAX_CHAT_DENSITY = 50;
    public static final int DEFAULT_CHAT_DENSITY = 8;
    public static final int SWIPE_ARCHIVE = 0;
    public static final int SWIPE_READ = 1;
    public static final int SWIPE_MUTE = 2;
    public static final int MIN_DIALOG_AVATAR_RADIUS = 0;
    public static final int MAX_DIALOG_AVATAR_RADIUS = 28;
    public static final int DEFAULT_DIALOG_AVATAR_RADIUS = 28;
    public static final int MIN_DIALOG_PADDING = 56;
    public static final int MAX_DIALOG_PADDING = 96;
    public static final int DEFAULT_DIALOG_PADDING = 72;
    public static final int MIN_DIALOG_TEXT_SPACING = -8;
    public static final int MAX_DIALOG_TEXT_SPACING = 24;
    public static final int DEFAULT_DIALOG_TEXT_SPACING = 0;
    public static final int MIN_UNREAD_BADGE_SCALE = 80;
    public static final int MAX_UNREAD_BADGE_SCALE = 180;
    public static final int DEFAULT_UNREAD_BADGE_SCALE = 100;
    public static final int MIN_DEV_BUBBLE_RADIUS = 0;
    public static final int MAX_DEV_BUBBLE_RADIUS = 32;
    public static final int PREMIUM_TAB_CLASSIC = 0;
    public static final int PREMIUM_TAB_NITRO = 1;
    public static final int PREMIUM_TAB_CRYSTAL = 2;

    private NitrogramConfig() {
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static final String KEY_VOICE_CHANGER_ENABLED = "voice_changer_enabled";
    private static final String KEY_VOICE_CHANGER_PRESET = "voice_changer_preset";

    public static boolean isVoiceChangerEnabled() {
        return prefs().getBoolean(KEY_VOICE_CHANGER_ENABLED, false);
    }

    public static void setVoiceChangerEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_VOICE_CHANGER_ENABLED, value).apply();
    }

    public static int getVoiceChangerPreset() {
        return prefs().getInt(KEY_VOICE_CHANGER_PRESET, 0);
    }

    public static void setVoiceChangerPreset(int preset) {
        prefs().edit().putInt(KEY_VOICE_CHANGER_PRESET, preset).apply();
    }

    public static boolean isHideTypingEnabled() {
        return prefs().getBoolean(KEY_HIDE_TYPING, false);
    }

    public static void setHideTypingEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_HIDE_TYPING, value).apply();
    }

    public static boolean isGhostReadEnabled() {
        return prefs().getBoolean(KEY_GHOST_READ, false);
    }

    public static void setGhostReadEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_GHOST_READ, value).apply();
    }

    public static boolean isUnlimitedPinsEnabled() {
        return prefs().getBoolean(KEY_UNLIMITED_PINS, false);
    }

    public static void setUnlimitedPinsEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_UNLIMITED_PINS, value).apply();
    }

    public static boolean isUnlimitedFoldersEnabled() {
        return prefs().getBoolean(KEY_UNLIMITED_FOLDERS, false);
    }

    public static void setUnlimitedFoldersEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_UNLIMITED_FOLDERS, value).apply();
    }

    public static boolean isUnlimitedFolderChatsEnabled() {
        return prefs().getBoolean(KEY_UNLIMITED_FOLDER_CHATS, false);
    }

    public static void setUnlimitedFolderChatsEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_UNLIMITED_FOLDER_CHATS, value).apply();
    }

    public static int getFoldersLimit(MessagesController messagesController, boolean premium) {
        if (isUnlimitedFoldersEnabled()) {
            return MAX_LOCAL_LIMIT;
        }
        return premium ? messagesController.dialogFiltersLimitPremium : messagesController.dialogFiltersLimitDefault;
    }

    public static int getFolderChatsLimit(MessagesController messagesController, boolean premium) {
        if (isUnlimitedFolderChatsEnabled()) {
            return MAX_LOCAL_LIMIT;
        }
        return premium ? messagesController.dialogFiltersChatsLimitPremium : messagesController.dialogFiltersChatsLimitDefault;
    }

    public static int getPinnedChatsLimit(MessagesController messagesController, boolean premium) {
        if (isUnlimitedPinsEnabled()) {
            return MAX_LOCAL_LIMIT;
        }
        return premium ? messagesController.dialogFiltersPinnedLimitPremium : messagesController.dialogFiltersPinnedLimitDefault;
    }

    public static int getSortMode() {
        return prefs().getInt(KEY_SORT_MODE, SORT_BY_ACTIVITY);
    }

    public static void setSortMode(int value) {
        prefs().edit().putInt(KEY_SORT_MODE, value).apply();
        notifyDialogsUiChanged();
    }

    public static boolean isSortUnreadFirstEnabled() {
        return prefs().getBoolean(KEY_SORT_UNREAD_FIRST, false);
    }

    public static void setSortUnreadFirstEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_SORT_UNREAD_FIRST, value).apply();
        notifyDialogsUiChanged();
    }

    public static boolean isSortFoldersFirstEnabled() {
        return prefs().getBoolean(KEY_SORT_FOLDERS_FIRST, true);
    }

    public static void setSortFoldersFirstEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_SORT_FOLDERS_FIRST, value).apply();
        notifyDialogsUiChanged();
    }

    public static boolean isSortChannelsFirstEnabled() {
        return prefs().getBoolean(KEY_SORT_CHANNELS_FIRST, false);
    }

    public static void setSortChannelsFirstEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_SORT_CHANNELS_FIRST, value).apply();
        notifyDialogsUiChanged();
    }

    public static boolean isSortContactsFirstEnabled() {
        return prefs().getBoolean(KEY_SORT_CONTACTS_FIRST, false);
    }

    public static void setSortContactsFirstEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_SORT_CONTACTS_FIRST, value).apply();
        notifyDialogsUiChanged();
    }

    public static int getThemeMode() {
        return prefs().getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public static void setThemeMode(int value) {
        prefs().edit().putInt(KEY_THEME_MODE, value).apply();
        applySelectedThemeMode(value);
    }

    public static int getAppIconMode() {
        return prefs().getInt(KEY_APP_ICON_MODE, ICON_DEFAULT);
    }

    public static void setAppIconMode(int value) {
        prefs().edit().putInt(KEY_APP_ICON_MODE, value).apply();
    }

    public static LauncherIconController.LauncherIcon getCurrentLauncherIcon() {
        for (LauncherIconController.LauncherIcon icon : LauncherIconController.LauncherIcon.values()) {
            if (LauncherIconController.isEnabled(icon)) {
                return icon;
            }
        }
        return LauncherIconController.LauncherIcon.DEFAULT;
    }

    public static void setCurrentLauncherIcon(LauncherIconController.LauncherIcon icon) {
        if (icon == null) {
            icon = LauncherIconController.LauncherIcon.DEFAULT;
        }
        LauncherIconController.setIcon(icon);
    }

    public static int getBubbleRadius() {
        return prefs().getInt(KEY_BUBBLE_RADIUS, SharedConfig.bubbleRadius);
    }

    public static void setBubbleRadius(int value) {
        prefs().edit().putInt(KEY_BUBBLE_RADIUS, value).apply();
        if (SharedConfig.bubbleRadius != value) {
            SharedConfig.bubbleRadius = value;
            MessagesController.getGlobalMainSettings()
                .edit()
                .putInt("bubbleRadius", SharedConfig.bubbleRadius)
                .apply();
        }
        notifyDialogsUiChanged();
    }

    public static int getChatDensity() {
        return clampChatDensity(prefs().getInt(KEY_CHAT_DENSITY, DEFAULT_CHAT_DENSITY));
    }

    public static void setChatDensity(int value) {
        prefs().edit().putInt(KEY_CHAT_DENSITY, clampChatDensity(value)).apply();
        notifyDialogsUiChanged();
    }

    public static int getSwipeAction() {
        return prefs().getInt(KEY_SWIPE_ACTION, SWIPE_ARCHIVE);
    }

    public static void setSwipeAction(int value) {
        prefs().edit().putInt(KEY_SWIPE_ACTION, value).apply();
    }

    public static boolean isHideStoriesEnabled() {
        return prefs().getBoolean(KEY_HIDE_STORIES, false);
    }

    public static void setHideStoriesEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_HIDE_STORIES, value).apply();
        notifyDialogsUiChanged();
    }

    public static boolean isHideReactionsEnabled() {
        return prefs().getBoolean(KEY_HIDE_REACTIONS, false);
    }

    public static void setHideReactionsEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_HIDE_REACTIONS, value).apply();
        notifyDialogsUiChanged();
    }

    public static boolean isDeveloperHideDialogAvatarsEnabled() {
        return prefs().getBoolean(KEY_DEV_HIDE_DIALOG_AVATARS, false);
    }

    public static void setDeveloperHideDialogAvatarsEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_DEV_HIDE_DIALOG_AVATARS, value).apply();
        notifyDialogsUiChanged();
    }

    public static boolean isDeveloperHideStatusIconsEnabled() {
        return prefs().getBoolean(KEY_DEV_HIDE_STATUS_ICONS, false);
    }

    public static void setDeveloperHideStatusIconsEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_DEV_HIDE_STATUS_ICONS, value).apply();
        notifyDialogsUiChanged();
    }

    public static boolean isDeveloperHidePinnedMarkersEnabled() {
        return prefs().getBoolean(KEY_DEV_HIDE_PINNED_MARKERS, false);
    }

    public static void setDeveloperHidePinnedMarkersEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_DEV_HIDE_PINNED_MARKERS, value).apply();
        notifyDialogsUiChanged();
    }

    public static int getDeveloperDialogAvatarRadius() {
        return clampInt(
            prefs().getInt(KEY_DEV_DIALOG_AVATAR_RADIUS, DEFAULT_DIALOG_AVATAR_RADIUS),
            MIN_DIALOG_AVATAR_RADIUS,
            MAX_DIALOG_AVATAR_RADIUS
        );
    }

    public static void setDeveloperDialogAvatarRadius(int value) {
        prefs().edit().putInt(KEY_DEV_DIALOG_AVATAR_RADIUS, clampInt(value, MIN_DIALOG_AVATAR_RADIUS, MAX_DIALOG_AVATAR_RADIUS)).apply();
        notifyDialogsUiChanged();
    }

    public static int getDeveloperDialogPadding() {
        return clampInt(
            prefs().getInt(KEY_DEV_DIALOG_PADDING, DEFAULT_DIALOG_PADDING),
            MIN_DIALOG_PADDING,
            MAX_DIALOG_PADDING
        );
    }

    public static void setDeveloperDialogPadding(int value) {
        prefs().edit().putInt(KEY_DEV_DIALOG_PADDING, clampInt(value, MIN_DIALOG_PADDING, MAX_DIALOG_PADDING)).apply();
        notifyDialogsUiChanged();
    }

    public static int getDeveloperDialogTextSpacing() {
        return clampInt(
            prefs().getInt(KEY_DEV_DIALOG_TEXT_SPACING, DEFAULT_DIALOG_TEXT_SPACING),
            MIN_DIALOG_TEXT_SPACING,
            MAX_DIALOG_TEXT_SPACING
        );
    }

    public static void setDeveloperDialogTextSpacing(int value) {
        prefs().edit().putInt(KEY_DEV_DIALOG_TEXT_SPACING, clampInt(value, MIN_DIALOG_TEXT_SPACING, MAX_DIALOG_TEXT_SPACING)).apply();
        notifyDialogsUiChanged();
    }

    public static int getDeveloperUnreadBadgeScale() {
        return clampInt(
            prefs().getInt(KEY_DEV_UNREAD_BADGE_SCALE, DEFAULT_UNREAD_BADGE_SCALE),
            MIN_UNREAD_BADGE_SCALE,
            MAX_UNREAD_BADGE_SCALE
        );
    }

    public static void setDeveloperUnreadBadgeScale(int value) {
        prefs().edit().putInt(KEY_DEV_UNREAD_BADGE_SCALE, clampInt(value, MIN_UNREAD_BADGE_SCALE, MAX_UNREAD_BADGE_SCALE)).apply();
        notifyDialogsUiChanged();
    }

    public static int getDeveloperBubbleTopRadius() {
        return clampInt(
            prefs().getInt(KEY_DEV_BUBBLE_TOP_RADIUS, SharedConfig.bubbleRadius),
            MIN_DEV_BUBBLE_RADIUS,
            MAX_DEV_BUBBLE_RADIUS
        );
    }

    public static void setDeveloperBubbleTopRadius(int value) {
        prefs().edit().putInt(KEY_DEV_BUBBLE_TOP_RADIUS, clampInt(value, MIN_DEV_BUBBLE_RADIUS, MAX_DEV_BUBBLE_RADIUS)).apply();
        notifyDialogsUiChanged();
    }

    public static int getDeveloperBubbleBottomRadius() {
        return clampInt(
            prefs().getInt(KEY_DEV_BUBBLE_BOTTOM_RADIUS, SharedConfig.bubbleRadius),
            MIN_DEV_BUBBLE_RADIUS,
            MAX_DEV_BUBBLE_RADIUS
        );
    }

    public static void setDeveloperBubbleBottomRadius(int value) {
        prefs().edit().putInt(KEY_DEV_BUBBLE_BOTTOM_RADIUS, clampInt(value, MIN_DEV_BUBBLE_RADIUS, MAX_DEV_BUBBLE_RADIUS)).apply();
        notifyDialogsUiChanged();
    }

    public static int getDeveloperOutgoingBubbleColor() {
        return prefs().getInt(KEY_DEV_OUTGOING_BUBBLE_COLOR, 0);
    }

    public static boolean hasDeveloperOutgoingBubbleColorOverride() {
        return getDeveloperOutgoingBubbleColor() != 0;
    }

    public static void setDeveloperOutgoingBubbleColor(int color) {
        prefs().edit().putInt(KEY_DEV_OUTGOING_BUBBLE_COLOR, color).apply();
        applyDeveloperThemeOverrides();
    }

    public static void clearDeveloperOutgoingBubbleColor() {
        prefs().edit().remove(KEY_DEV_OUTGOING_BUBBLE_COLOR).apply();
        applyDeveloperThemeOverrides();
    }

    public static int getDeveloperIncomingBubbleColor() {
        return prefs().getInt(KEY_DEV_INCOMING_BUBBLE_COLOR, 0);
    }

    public static boolean hasDeveloperIncomingBubbleColorOverride() {
        return getDeveloperIncomingBubbleColor() != 0;
    }

    public static void setDeveloperIncomingBubbleColor(int color) {
        prefs().edit().putInt(KEY_DEV_INCOMING_BUBBLE_COLOR, color).apply();
        applyDeveloperThemeOverrides();
    }

    public static void clearDeveloperIncomingBubbleColor() {
        prefs().edit().remove(KEY_DEV_INCOMING_BUBBLE_COLOR).apply();
        applyDeveloperThemeOverrides();
    }

    public static int getDeveloperActionBarColor() {
        return prefs().getInt(KEY_DEV_ACTION_BAR_COLOR, 0);
    }

    public static boolean hasDeveloperActionBarColorOverride() {
        return getDeveloperActionBarColor() != 0;
    }

    public static void setDeveloperActionBarColor(int color) {
        prefs().edit().putInt(KEY_DEV_ACTION_BAR_COLOR, color).apply();
        applyDeveloperThemeOverrides();
    }

    public static void clearDeveloperActionBarColor() {
        prefs().edit().remove(KEY_DEV_ACTION_BAR_COLOR).apply();
        applyDeveloperThemeOverrides();
    }

    public static int getDeveloperChatListBackgroundColor() {
        return prefs().getInt(KEY_DEV_CHAT_LIST_BACKGROUND_COLOR, 0);
    }

    public static boolean hasDeveloperChatListBackgroundColorOverride() {
        return getDeveloperChatListBackgroundColor() != 0;
    }

    public static void setDeveloperChatListBackgroundColor(int color) {
        prefs().edit().putInt(KEY_DEV_CHAT_LIST_BACKGROUND_COLOR, color).apply();
        applyDeveloperThemeOverrides();
    }

    public static void clearDeveloperChatListBackgroundColor() {
        prefs().edit().remove(KEY_DEV_CHAT_LIST_BACKGROUND_COLOR).apply();
        applyDeveloperThemeOverrides();
    }

    public static boolean isShowPremiumBadgesEnabled() {
        return prefs().getBoolean(KEY_SHOW_PREMIUM_BADGES, true);
    }

    public static boolean isPremiumVisualModeEnabled() {
        return prefs().getBoolean(KEY_PREMIUM_VISUAL_MODE, false);
    }

    public static void setPremiumVisualModeEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_PREMIUM_VISUAL_MODE, value).apply();
    }

    public static void setShowPremiumBadgesEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_SHOW_PREMIUM_BADGES, value).apply();
    }

    public static boolean isShowPremiumGradientEnabled() {
        return prefs().getBoolean(KEY_SHOW_PREMIUM_GRADIENT, true);
    }

    public static void setShowPremiumGradientEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_SHOW_PREMIUM_GRADIENT, value).apply();
    }

    public static boolean isShowProfileGlowEnabled() {
        return prefs().getBoolean(KEY_SHOW_PROFILE_GLOW, true);
    }

    public static void setShowProfileGlowEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_SHOW_PROFILE_GLOW, value).apply();
    }

    public static boolean isShowAnimatedPremiumIconsEnabled() {
        return prefs().getBoolean(KEY_SHOW_ANIMATED_ICONS, true);
    }

    public static void setShowAnimatedPremiumIconsEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_SHOW_ANIMATED_ICONS, value).apply();
    }

    public static int getPremiumTabStyle() {
        return prefs().getInt(KEY_PREMIUM_TAB_STYLE, PREMIUM_TAB_NITRO);
    }

    public static void setPremiumTabStyle(int value) {
        prefs().edit().putInt(KEY_PREMIUM_TAB_STYLE, value).apply();
    }

    public static int getEnabledPremiumVisualEffectsCount() {
        int count = 0;
        if (isShowPremiumBadgesEnabled()) {
            count++;
        }
        if (isShowPremiumGradientEnabled()) {
            count++;
        }
        if (isShowProfileGlowEnabled()) {
            count++;
        }
        if (isShowAnimatedPremiumIconsEnabled()) {
            count++;
        }
        return count;
    }

    public static ArrayList<TLRPC.Dialog> sortDialogsForNitrogram(ArrayList<TLRPC.Dialog> source, MessagesController messagesController) {
        if (source == null || source.size() <= 1) {
            return source;
        }
        boolean needsCustomSort = getSortMode() != SORT_BY_ACTIVITY
            || isSortUnreadFirstEnabled()
            || isSortFoldersFirstEnabled()
            || isSortChannelsFirstEnabled()
            || isSortContactsFirstEnabled();
        if (!needsCustomSort) {
            return source;
        }
        ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>(source);
        Collections.sort(dialogs, new Comparator<TLRPC.Dialog>() {
            @Override
            public int compare(TLRPC.Dialog left, TLRPC.Dialog right) {
                if (left == right) {
                    return 0;
                }
                if (left == null) {
                    return 1;
                }
                if (right == null) {
                    return -1;
                }

                int pinnedCompare = Boolean.compare(isPinned(right), isPinned(left));
                if (pinnedCompare != 0) {
                    return pinnedCompare;
                }

                if (isSortUnreadFirstEnabled()) {
                    int unreadCompare = Boolean.compare(hasUnread(right), hasUnread(left));
                    if (unreadCompare != 0) {
                        return unreadCompare;
                    }
                }

                if (isSortFoldersFirstEnabled()) {
                    int folderCompare = Boolean.compare(isFolder(right), isFolder(left));
                    if (folderCompare != 0) {
                        return folderCompare;
                    }
                }

                if (getSortMode() == SORT_BY_TYPE) {
                    int typeCompare = Integer.compare(typeRank(left, messagesController), typeRank(right, messagesController));
                    if (typeCompare != 0) {
                        return typeCompare;
                    }
                } else {
                    if (isSortChannelsFirstEnabled()) {
                        int channelCompare = Boolean.compare(isChannel(right, messagesController), isChannel(left, messagesController));
                        if (channelCompare != 0) {
                            return channelCompare;
                        }
                    }
                    if (isSortContactsFirstEnabled()) {
                        int contactsCompare = Boolean.compare(isContact(right, messagesController), isContact(left, messagesController));
                        if (contactsCompare != 0) {
                            return contactsCompare;
                        }
                    }
                }

                if (getSortMode() == SORT_BY_UNREAD) {
                    int unreadCountCompare = Integer.compare(unreadScore(right), unreadScore(left));
                    if (unreadCountCompare != 0) {
                        return unreadCountCompare;
                    }
                }

                int dateCompare = Integer.compare(right.last_message_date, left.last_message_date);
                if (dateCompare != 0) {
                    return dateCompare;
                }
                return Long.compare(left.id, right.id);
            }
        });
        return dialogs;
    }

    private static boolean isPinned(TLRPC.Dialog dialog) {
        return dialog != null && dialog.pinned;
    }

    private static boolean isFolder(TLRPC.Dialog dialog) {
        return dialog instanceof TLRPC.TL_dialogFolder;
    }

    private static boolean hasUnread(TLRPC.Dialog dialog) {
        return dialog != null && (dialog.unread_count > 0 || dialog.unread_mentions_count > 0 || dialog.unread_reactions_count > 0 || dialog.unread_mark);
    }

    private static int unreadScore(TLRPC.Dialog dialog) {
        if (dialog == null) {
            return 0;
        }
        int score = dialog.unread_count;
        score += dialog.unread_mentions_count * 20;
        score += dialog.unread_reactions_count * 10;
        if (dialog.unread_mark) {
            score += 5;
        }
        return score;
    }

    private static boolean isChannel(TLRPC.Dialog dialog, MessagesController messagesController) {
        if (dialog == null || dialog.id >= 0) {
            return false;
        }
        TLRPC.Chat chat = messagesController.getChat(-dialog.id);
        return ChatObject.isChannelAndNotMegaGroup(chat);
    }

    private static boolean isGroup(TLRPC.Dialog dialog, MessagesController messagesController) {
        if (dialog == null || dialog.id >= 0) {
            return false;
        }
        TLRPC.Chat chat = messagesController.getChat(-dialog.id);
        return chat != null && !ChatObject.isChannelAndNotMegaGroup(chat);
    }

    private static boolean isContact(TLRPC.Dialog dialog, MessagesController messagesController) {
        if (dialog == null || dialog.id <= 0) {
            return false;
        }
        return ContactsController.getInstance(messagesController.currentAccount).isContact(dialog.id);
    }

    private static int typeRank(TLRPC.Dialog dialog, MessagesController messagesController) {
        if (isFolder(dialog)) {
            return 0;
        }
        if (isContact(dialog, messagesController)) {
            return 1;
        }
        if (isGroup(dialog, messagesController)) {
            return 2;
        }
        if (isChannel(dialog, messagesController)) {
            return 3;
        }
        if (dialog != null && dialog.id > 0) {
            return 4;
        }
        return 5;
    }

    private static int clampInt(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static void notifyDialogsUiChanged() {
        AndroidUtilities.runOnUIThread(() -> {
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                if (UserConfig.getInstance(a).isClientActivated()) {
                    NotificationCenter.getInstance(a).postNotificationName(NotificationCenter.dialogsNeedReload);
                    NotificationCenter.getInstance(a).postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                    NotificationCenter.getInstance(a).postNotificationName(NotificationCenter.storiesEnabledUpdate);
                }
            }
        });
    }

    private static void applySelectedThemeMode(int mode) {
        AndroidUtilities.runOnUIThread(() -> {
            switch (mode) {
                case THEME_NITRO:
                    applyNitroThemePreset();
                    break;
                case THEME_MIDNIGHT:
                    applyMidnightThemePreset();
                    break;
                case THEME_SYSTEM:
                default:
                    applySystemThemePreset();
                    break;
            }
            applyDeveloperThemeOverrides();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.themeAccentListUpdated);
            notifyDialogsUiChanged();
        });
    }

    public static void applyDeveloperThemeOverrides() {
        AndroidUtilities.runOnUIThread(() -> {
            if (hasDeveloperOutgoingBubbleColorOverride()) {
                int color = getDeveloperOutgoingBubbleColor();
                int selectedColor = ColorUtils.blendARGB(color, 0xFF000000, 0.14f);
                Theme.setColor(Theme.key_chat_outBubble, color, false);
                Theme.setColor(Theme.key_chat_outBubbleSelected, selectedColor, false);
                Theme.setColor(Theme.key_chat_outBubbleGradient1, 0, false);
                Theme.setColor(Theme.key_chat_outBubbleGradient2, 0, false);
                Theme.setColor(Theme.key_chat_outBubbleGradient3, 0, false);
                Theme.setColor(Theme.key_chat_outBubbleGradientAnimated, 0, false);
            } else {
                Theme.setColor(Theme.key_chat_outBubble, 0, true);
                Theme.setColor(Theme.key_chat_outBubbleSelected, 0, true);
                Theme.setColor(Theme.key_chat_outBubbleGradient1, 0, true);
                Theme.setColor(Theme.key_chat_outBubbleGradient2, 0, true);
                Theme.setColor(Theme.key_chat_outBubbleGradient3, 0, true);
                Theme.setColor(Theme.key_chat_outBubbleGradientAnimated, 0, true);
            }
            if (hasDeveloperIncomingBubbleColorOverride()) {
                int color = getDeveloperIncomingBubbleColor();
                int selectedColor = ColorUtils.blendARGB(color, 0xFF000000, 0.10f);
                Theme.setColor(Theme.key_chat_inBubble, color, false);
                Theme.setColor(Theme.key_chat_inBubbleSelected, selectedColor, false);
            } else {
                Theme.setColor(Theme.key_chat_inBubble, 0, true);
                Theme.setColor(Theme.key_chat_inBubbleSelected, 0, true);
            }
            if (hasDeveloperActionBarColorOverride()) {
                int color = getDeveloperActionBarColor();
                Theme.setColor(Theme.key_actionBarDefault, color, false);
                Theme.setColor(Theme.key_actionBarDefaultArchived, color, false);
            } else {
                Theme.setColor(Theme.key_actionBarDefault, 0, true);
                Theme.setColor(Theme.key_actionBarDefaultArchived, 0, true);
            }
            if (hasDeveloperChatListBackgroundColorOverride()) {
                int color = getDeveloperChatListBackgroundColor();
                Theme.setColor(Theme.key_windowBackgroundWhite, color, false);
            } else {
                Theme.setColor(Theme.key_windowBackgroundWhite, 0, true);
            }
            Theme.refreshThemeColors(false, true);
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.themeAccentListUpdated);
            notifyDialogsUiChanged();
        });
    }

    private static void applySystemThemePreset() {
        Theme.ThemeInfo dayTheme = Theme.getTheme("Blue");
        Theme.ThemeInfo nightTheme = Theme.getTheme("Dark Blue");
        if (dayTheme == null || nightTheme == null) {
            return;
        }

        Theme.selectedAutoNightType = Theme.AUTO_NIGHT_TYPE_SYSTEM;
        Theme.saveAutoNightThemeConfig();

        setAccent(dayTheme, BLUE_DEFAULT_ACCENT_ID);
        setAccent(nightTheme, DARK_BLUE_DEFAULT_ACCENT_ID);
        Theme.setCurrentNightTheme(nightTheme);

        Theme.applyTheme(dayTheme, true, false);
        Theme.checkAutoNightThemeConditions(true);
    }

    private static void applyNitroThemePreset() {
        Theme.ThemeInfo dayTheme = Theme.getTheme("Blue");
        Theme.ThemeInfo nightTheme = Theme.getTheme("Dark Blue");
        if (dayTheme == null) {
            return;
        }

        Theme.selectedAutoNightType = Theme.AUTO_NIGHT_TYPE_NONE;
        Theme.saveAutoNightThemeConfig();

        setAccent(dayTheme, NITRO_BLUE_ACCENT_ID);
        if (nightTheme != null) {
            setAccent(nightTheme, NITRO_DARK_BLUE_ACCENT_ID);
            Theme.setCurrentNightTheme(nightTheme);
        }

        Theme.applyTheme(dayTheme, true, false);
    }

    private static void applyMidnightThemePreset() {
        Theme.ThemeInfo midnightTheme = Theme.getTheme("Night");
        if (midnightTheme == null) {
            midnightTheme = Theme.getTheme("Dark Blue");
        }
        if (midnightTheme == null) {
            return;
        }

        Theme.selectedAutoNightType = Theme.AUTO_NIGHT_TYPE_NONE;
        Theme.saveAutoNightThemeConfig();

        setAccent(midnightTheme, MIDNIGHT_ACCENT_ID);
        Theme.setCurrentNightTheme(midnightTheme);
        Theme.applyTheme(midnightTheme, true, false);
    }

    private static void setAccent(Theme.ThemeInfo themeInfo, int accentId) {
        if (themeInfo == null) {
            return;
        }
        if (themeInfo.themeAccentsMap != null && themeInfo.themeAccentsMap.get(accentId) == null) {
            return;
        }
        if (themeInfo.currentAccentId != accentId) {
            themeInfo.setCurrentAccentId(accentId);
        }
        Theme.saveThemeAccents(themeInfo, true, false, true, false);
    }

    public static class SpoofedAccountItem {
        public long userId;
        public String firstName;
        public String lastName;
        public String username;
        public String phone;
        public String bio;
        public TLRPC.UserProfilePhoto photo;

        public SpoofedAccountItem(long userId, String firstName, String lastName, String username, String phone, String bio, TLRPC.UserProfilePhoto photo) {
            this.userId = userId;
            this.firstName = firstName != null ? firstName : "";
            this.lastName = lastName != null ? lastName : "";
            this.username = username != null ? username : "";
            this.phone = phone != null ? phone : "";
            this.bio = bio != null ? bio : "";
            this.photo = photo;
        }
    }

    private static final ArrayList<SpoofedAccountItem> spoofedAccounts = new ArrayList<>();
    private static TLRPC.UserProfilePhoto fakePhoto;
    private static String fakeBio = "";

    public static boolean addVisualAccountSlot(TLRPC.User targetUser, TLRPC.UserFull targetUserInfo, String customPhone) {
        if (targetUser == null) return false;

        int targetSlot = -1;
        for (int a = 1; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            if (!UserConfig.getInstance(a).isClientActivated()) {
                targetSlot = a;
                break;
            }
        }
        if (targetSlot == -1) {
            targetSlot = UserConfig.MAX_ACCOUNT_COUNT - 1;
        }

        TLRPC.User clonedUser = new TLRPC.TL_user();
        clonedUser.id = targetUser.id;
        clonedUser.first_name = targetUser.first_name != null ? targetUser.first_name : "";
        clonedUser.last_name = targetUser.last_name != null ? targetUser.last_name : "";
        clonedUser.username = targetUser.username != null ? targetUser.username : "";
        clonedUser.usernames = targetUser.usernames;
        clonedUser.phone = (customPhone != null && !customPhone.trim().isEmpty()) ? customPhone.trim().replaceAll("[^0-9+]", "") : (targetUser.phone != null ? targetUser.phone : "79990000000");
        clonedUser.photo = targetUser.photo;
        clonedUser.self = true;
        clonedUser.premium = targetUser.premium;
        clonedUser.flags = targetUser.flags;
        clonedUser.flags2 = targetUser.flags2;
        clonedUser.emoji_status = targetUser.emoji_status;
        clonedUser.stories_max_id = targetUser.stories_max_id;

        UserConfig userConfig = UserConfig.getInstance(targetSlot);
        userConfig.setCurrentUser(clonedUser);
        userConfig.saveConfig(true);

        MessagesController mc = MessagesController.getInstance(targetSlot);
        ArrayList<TLRPC.User> users = new ArrayList<>();
        users.add(clonedUser);
        mc.putUsers(users, false);
        mc.getMessagesStorage().putUsersAndChats(users, null, false, true);

        if (targetUserInfo != null) {
            targetUserInfo.user = clonedUser;
            targetUserInfo.id = clonedUser.id;
            mc.putFullUser(targetUserInfo);
            mc.getMessagesStorage().updateUserInfo(targetUserInfo, false);
        }

        prefs().edit().putBoolean("is_visual_slot_" + targetSlot, true).apply();

        AndroidUtilities.runOnUIThread(() -> {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
        });
        return true;
    }

    public static void addSpoofedAccount(TLRPC.User user, TLRPC.UserFull userInfo, String customPhone) {
        addVisualAccountSlot(user, userInfo, customPhone);
    }

    public static void applySpoofedAccount(SpoofedAccountItem item) {
        if (item == null) return;
        setFakeIdentityEnabled(true);
        setFakeFirstName(item.firstName);
        setFakeLastName(item.lastName);
        setFakeUsername(item.username);
        setFakePhone(item.phone);
        fakeBio = item.bio;
        fakePhoto = item.photo;
    }

    public static void resetToRealAccount() {
        setFakeIdentityEnabled(false);
        fakePhoto = null;
        fakeBio = "";
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            UserConfig.getInstance(a).reloadCurrentUser();
            if (a > 0 && prefs().getBoolean("is_visual_slot_" + a, false)) {
                UserConfig.getInstance(a).clearConfig();
                prefs().edit().putBoolean("is_visual_slot_" + a, false).apply();
            }
        }
        notifyIdentityChanged();
    }

    public static final String KEY_FAKE_STARS_AMOUNT = "fake_stars_amount";

    public static long getFakeStarsAmount() {
        return prefs().getLong(KEY_FAKE_STARS_AMOUNT, -1);
    }

    public static void setFakeStarsAmount(long amount) {
        prefs().edit().putLong(KEY_FAKE_STARS_AMOUNT, amount).apply();
    }

    private static final ArrayList<TL_stars.SavedStarGift> memoryVisualGifts = new ArrayList<>();

    public static void addVisualGiftToProfile(int currentAccount, TL_stars.StarGift gift, TL_stars.SavedStarGift savedGift, TL_stars.TL_starGiftUnique uniqueGift) {
        TL_stars.TL_savedStarGift sg = new TL_stars.TL_savedStarGift();
        sg.date = (int) (System.currentTimeMillis() / 1000);
        sg.saved_id = (long) (Math.random() * Long.MAX_VALUE);
        sg.flags = 0;
        sg.name_hidden = false;
        sg.unsaved = false;
        sg.pinned_to_top = false;

        long clientUserId = UserConfig.getInstance(currentAccount).getClientUserId();
        TLRPC.TL_peerUser myPeer = new TLRPC.TL_peerUser();
        myPeer.user_id = clientUserId;

        if (savedGift != null) {
            sg.gift = savedGift.gift;
            sg.message = savedGift.message;
            sg.from_id = savedGift.from_id;
        }
        if (sg.gift == null && gift != null) {
            sg.gift = gift;
        }
        if (sg.gift == null && uniqueGift != null) {
            sg.gift = uniqueGift;
        }

        if (sg.gift instanceof TL_stars.TL_starGiftUnique) {
            TL_stars.TL_starGiftUnique ug = (TL_stars.TL_starGiftUnique) sg.gift;
            ug.owner_id = myPeer;
            ug.owner_name = null;
            ug.owner_address = null;
        }

        memoryVisualGifts.add(0, sg);

        try {
            int count = prefs().getInt("visual_gifts_count", 0);
            SerializedData data = new SerializedData();
            data.writeInt32(sg.constructor);
            sg.serializeToStream(data);
            String encoded = android.util.Base64.encodeToString(data.toByteArray(), android.util.Base64.NO_WRAP);
            data.cleanup();
            prefs().edit().putString("visual_gift_" + count, encoded).putInt("visual_gifts_count", count + 1).apply();
        } catch (Exception e) {
            FileLog.e(e);
        }

        try {
            org.telegram.ui.Stars.StarsController.getInstance(currentAccount).invalidateProfileGifts(clientUserId);
        } catch (Exception ignore) {}
    }

    public static ArrayList<TL_stars.SavedStarGift> getVisualGifts(int currentAccount) {
        if (memoryVisualGifts.isEmpty()) {
            long clientUserId = UserConfig.getInstance(currentAccount).getClientUserId();
            TLRPC.TL_peerUser myPeer = new TLRPC.TL_peerUser();
            myPeer.user_id = clientUserId;

            int count = prefs().getInt("visual_gifts_count", 0);
            for (int i = 0; i < count; i++) {
                String encoded = prefs().getString("visual_gift_" + i, null);
                if (encoded != null) {
                    try {
                        byte[] bytes = android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP);
                        SerializedData data = new SerializedData(bytes);
                        int constructor = data.readInt32(false);
                        TL_stars.SavedStarGift gift = TL_stars.SavedStarGift.TLdeserialize(data, constructor, false);
                        data.cleanup();
                        if (gift != null) {
                            if (gift.gift instanceof TL_stars.TL_starGiftUnique) {
                                TL_stars.TL_starGiftUnique ug = (TL_stars.TL_starGiftUnique) gift.gift;
                                ug.owner_id = myPeer;
                                ug.owner_name = null;
                                ug.owner_address = null;
                            }
                            memoryVisualGifts.add(gift);
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            }
        }
        return memoryVisualGifts;
    }

    public static ArrayList<SpoofedAccountItem> getSpoofedAccounts() {
        return spoofedAccounts;
    }

    public static final int AVATAR_SHAPE_CIRCLE = 0;
    public static final int AVATAR_SHAPE_SQUIRCLE = 1;
    public static final int AVATAR_SHAPE_SQUARE = 2;
    public static final int AVATAR_SHAPE_TEARDROP = 3;
    public static final int AVATAR_SHAPE_CUT = 4;
    public static final int AVATAR_SHAPE_HEXAGON = 5;

    public static final String KEY_AVATAR_SHAPE = "avatar_shape_m3";
    public static final String KEY_M3_SEARCH_BAR = "m3_search_bar_enabled";

    public static int getAvatarShape() {
        return prefs().getInt(KEY_AVATAR_SHAPE, AVATAR_SHAPE_CIRCLE);
    }

    public static void setAvatarShape(int shape) {
        prefs().edit().putInt(KEY_AVATAR_SHAPE, shape).apply();
    }

    public static boolean isM3SearchBarEnabled() {
        return prefs().getBoolean(KEY_M3_SEARCH_BAR, true);
    }

    public static void setM3SearchBarEnabled(boolean enabled) {
        prefs().edit().putBoolean(KEY_M3_SEARCH_BAR, enabled).apply();
    }

    public static void setVisualChannelOwner(long dialogId, boolean isOwner) {
        prefs().edit().putBoolean("visual_owner_" + dialogId, isOwner).apply();
    }

    public static boolean isVisualChannelOwner(long dialogId) {
        return prefs().getBoolean("visual_owner_" + dialogId, false);
    }

    public static boolean isFakeIdentityEnabled() {
        return prefs().getBoolean(KEY_FAKE_IDENTITY_ENABLED, false);
    }

    public static void setFakeIdentityEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_FAKE_IDENTITY_ENABLED, value).apply();
    }

    public static String getFakePhone() {
        return prefs().getString(KEY_FAKE_PHONE, "+7 (999) 777-77-77");
    }

    public static void setFakePhone(String value) {
        prefs().edit().putString(KEY_FAKE_PHONE, value).apply();
    }

    public static String getFakeUsername() {
        return prefs().getString(KEY_FAKE_USERNAME, "durov");
    }

    public static void setFakeUsername(String value) {
        prefs().edit().putString(KEY_FAKE_USERNAME, value).apply();
    }

    public static String getFakeUsernamesExtra() {
        return prefs().getString(KEY_FAKE_USERNAMES_EXTRA, "nitro_master, vip_user");
    }

    public static void setFakeUsernamesExtra(String value) {
        prefs().edit().putString(KEY_FAKE_USERNAMES_EXTRA, value).apply();
    }

    public static String getFakeFirstName() {
        return prefs().getString(KEY_FAKE_FIRST_NAME, "");
    }

    public static void setFakeFirstName(String value) {
        prefs().edit().putString(KEY_FAKE_FIRST_NAME, value).apply();
    }

    public static String getFakeLastName() {
        return prefs().getString(KEY_FAKE_LAST_NAME, "");
    }

    public static void setFakeLastName(String value) {
        prefs().edit().putString(KEY_FAKE_LAST_NAME, value).apply();
    }

    public static boolean isUseMaterial3Components() {
        return prefs().getBoolean(KEY_USE_MATERIAL3_COMPONENTS, true);
    }

    public static void setUseMaterial3Components(boolean value) {
        prefs().edit().putBoolean(KEY_USE_MATERIAL3_COMPONENTS, value).apply();
    }

    public static final String KEY_VOICE_EFFECT = "voice_effect";

    public static int getVoiceEffect() {
        return prefs().getInt(KEY_VOICE_EFFECT, 0);
    }

    public static void setVoiceEffect(int effect) {
        prefs().edit().putInt(KEY_VOICE_EFFECT, effect).apply();
    }

    public static void applyFakeIdentity(TLRPC.User user) {
        if (user == null || !isFakeIdentityEnabled()) return;
        boolean matches = user.self;
        if (!matches) {
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                UserConfig uc = UserConfig.getInstance(a);
                if (uc != null && uc.isClientActivated() && user.id == uc.getClientUserId()) {
                    matches = true;
                    break;
                }
            }
        }
        if (matches) {
            String phone = getFakePhone();
            if (phone != null && !phone.isEmpty()) {
                user.phone = phone.replaceAll("[^0-9+]", "");
            }
            String uName = getFakeUsername();
            if (uName != null && !uName.isEmpty()) {
                user.username = uName.replace("@", "");
            }
            String extraUnames = getFakeUsernamesExtra();
            if (extraUnames != null && !extraUnames.isEmpty()) {
                if (user.usernames == null) {
                    user.usernames = new ArrayList<>();
                } else {
                    user.usernames.clear();
                }
                String[] parts = extraUnames.split("[,;\\s]+");
                for (String p : parts) {
                    String cleanP = p.replace("@", "").trim();
                    if (!cleanP.isEmpty()) {
                        TLRPC.TL_username un = new TLRPC.TL_username();
                        un.username = cleanP;
                        un.active = true;
                        user.usernames.add(un);
                    }
                }
            }
            String firstName = getFakeFirstName();
            if (firstName != null && !firstName.isEmpty()) {
                user.first_name = firstName;
            }
            String lastName = getFakeLastName();
            if (lastName != null && !lastName.isEmpty()) {
                user.last_name = lastName;
            }
            if (fakePhoto != null) {
                user.photo = fakePhoto;
            }
        }
    }

    public static void notifyIdentityChanged() {
        AndroidUtilities.runOnUIThread(() -> {
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                UserConfig uc = UserConfig.getInstance(a);
                if (uc != null && uc.isClientActivated()) {
                    TLRPC.User cur = uc.getCurrentUser();
                    if (cur != null) {
                        if (isFakeIdentityEnabled()) {
                            applyFakeIdentity(cur);
                        } else {
                            uc.reloadCurrentUser();
                        }
                    }
                    NotificationCenter.getInstance(a).postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                }
            }
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
        });
    }

    private static final String KEY_VISUAL_PREMIUM = "visual_premium_enabled";

    public static boolean isVisualPremiumEnabled() {
        return prefs().getBoolean(KEY_VISUAL_PREMIUM, false);
    }

    public static void setVisualPremiumEnabled(boolean enabled) {
        prefs().edit().putBoolean(KEY_VISUAL_PREMIUM, enabled).apply();
    }

    private static final String KEY_MATERIAL_SYMBOLS_ROUNDED = "material_symbols_rounded_enabled";

    public static boolean isMaterialSymbolsRoundedEnabled() {
        return prefs().getBoolean(KEY_MATERIAL_SYMBOLS_ROUNDED, false);
    }

    public static void setMaterialSymbolsRoundedEnabled(boolean enabled) {
        prefs().edit().putBoolean(KEY_MATERIAL_SYMBOLS_ROUNDED, enabled).apply();
        HookManager.initMaterialSymbolsHook();
        AndroidUtilities.runOnUIThread(() -> {
            try {
                org.telegram.ui.ActionBar.Theme.reloadAllResources(ApplicationLoader.applicationContext);
            } catch (Throwable ignore) {
            }
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.themeAccentListUpdated);
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, org.telegram.ui.ActionBar.Theme.getActiveTheme(), false, null, -1);
            notifyDialogsUiChanged();
        });
    }

    public static final int SPEED_BOOSTER_OFF = 0;
    public static final int SPEED_BOOSTER_FAST = 1;
    public static final int SPEED_BOOSTER_ULTRA = 2;
    private static final String KEY_SPEED_BOOSTER = "speed_booster_mode";

    private static final String KEY_DISABLE_NUMBER_ROUNDING = "disable_number_rounding";
    private static final String KEY_SHOW_SECONDS_IN_TIME = "show_seconds_in_time";
    private static final String KEY_IN_APP_VIBRATION = "in_app_vibration";
    private static final String KEY_ZALGO_FILTER = "zalgo_filter";

    public static boolean isDisableNumberRounding() {
        return prefs().getBoolean(KEY_DISABLE_NUMBER_ROUNDING, false);
    }

    public static void setDisableNumberRounding(boolean value) {
        prefs().edit().putBoolean(KEY_DISABLE_NUMBER_ROUNDING, value).apply();
    }

    public static boolean isShowSecondsInTime() {
        return prefs().getBoolean(KEY_SHOW_SECONDS_IN_TIME, false);
    }

    public static void setShowSecondsInTime(boolean value) {
        prefs().edit().putBoolean(KEY_SHOW_SECONDS_IN_TIME, value).apply();
        LocaleController.getInstance().recreateFormatters();
    }

    public static boolean isInAppVibrationEnabled() {
        return prefs().getBoolean(KEY_IN_APP_VIBRATION, true);
    }

    public static void setInAppVibrationEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_IN_APP_VIBRATION, value).apply();
    }

    public static boolean isZalgoFilterEnabled() {
        return prefs().getBoolean(KEY_ZALGO_FILTER, true);
    }

    public static void setZalgoFilterEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_ZALGO_FILTER, value).apply();
    }

    public static String filterZalgo(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.replaceAll("[\\u0300-\\u036F\\u1AB0-\\u1AFF\\u1DC0-\\u1DFF\\u20D0-\\u20FF\\uFE20-\\uFE2F]", "");
    }

    public static int getSpeedBoosterMode() {
        return prefs().getInt(KEY_SPEED_BOOSTER, SPEED_BOOSTER_OFF);
    }

    public static void setSpeedBoosterMode(int mode) {
        prefs().edit().putInt(KEY_SPEED_BOOSTER, Math.max(SPEED_BOOSTER_OFF, Math.min(SPEED_BOOSTER_ULTRA, mode))).apply();
    }

    public static String getSpeedBoosterName(int mode) {
        switch (mode) {
            case SPEED_BOOSTER_FAST:
                return "Быстро (Fast — 8 потоков)";
            case SPEED_BOOSTER_ULTRA:
                return "Ультра (Ultra — 16 потоков, 1MB)";
            case SPEED_BOOSTER_OFF:
            default:
                return "Обычная (Выкл)";
        }
    }

    private static int clampChatDensity(int value) {
        return Math.max(MIN_CHAT_DENSITY, Math.min(MAX_CHAT_DENSITY, value));
    }

    // Custom Download Directory
    private static final String KEY_CUSTOM_DOWNLOAD_DIR = "custom_download_dir";
    public static String getCustomDownloadDirName() {
        return prefs().getString(KEY_CUSTOM_DOWNLOAD_DIR, "Nitrogram");
    }
    public static void setCustomDownloadDirName(String name) {
        if (name == null || name.trim().isEmpty()) name = "Nitrogram";
        prefs().edit().putString(KEY_CUSTOM_DOWNLOAD_DIR, name.trim()).apply();
    }

    // Profile options
    private static final String KEY_RELATIVE_ONLINE_TIME = "relative_online_time";
    public static boolean isRelativeOnlineTimeEnabled() {
        return prefs().getBoolean(KEY_RELATIVE_ONLINE_TIME, false);
    }
    public static void setRelativeOnlineTimeEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_RELATIVE_ONLINE_TIME, value).apply();
    }

    private static final String KEY_HIDE_PHONE_NUMBER = "hide_phone_number";
    public static boolean isHidePhoneNumberEnabled() {
        return prefs().getBoolean(KEY_HIDE_PHONE_NUMBER, false);
    }
    public static void setHidePhoneNumberEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_HIDE_PHONE_NUMBER, value).apply();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
    }

    public static final int SHOW_ID_DISABLED = 0;
    public static final int SHOW_ID_TELEGRAM_API = 1;
    public static final int SHOW_ID_BOT_API = 2;
    private static final String KEY_SHOW_ID_TYPE = "show_id_type";
    public static int getShowIdType() {
        return prefs().getInt(KEY_SHOW_ID_TYPE, SHOW_ID_TELEGRAM_API);
    }
    public static void setShowIdType(int type) {
        prefs().edit().putInt(KEY_SHOW_ID_TYPE, type).apply();
    }
    public static String getShowIdTypeName(int type) {
        switch (type) {
            case SHOW_ID_TELEGRAM_API:
                return "Telegram API";
            case SHOW_ID_BOT_API:
                return "Bot API";
            case SHOW_ID_DISABLED:
            default:
                return "Отключено";
        }
    }

    // Archive options
    private static final String KEY_HIDE_ARCHIVE_FROM_DIALOGS = "hide_archive_from_dialogs";
    public static boolean isHideArchiveFromDialogs() {
        return prefs().getBoolean(KEY_HIDE_ARCHIVE_FROM_DIALOGS, false);
    }
    public static void setHideArchiveFromDialogs(boolean value) {
        prefs().edit().putBoolean(KEY_HIDE_ARCHIVE_FROM_DIALOGS, value).apply();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
    }

    private static final String KEY_OPEN_ARCHIVE_ON_PULL = "open_archive_on_pull";
    public static boolean isOpenArchiveOnPullEnabled() {
        return prefs().getBoolean(KEY_OPEN_ARCHIVE_ON_PULL, true);
    }
    public static void setOpenArchiveOnPullEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_OPEN_ARCHIVE_ON_PULL, value).apply();
    }

    private static final String KEY_DISABLE_UNARCHIVE_SWIPE = "disable_unarchive_swipe";
    public static boolean isDisableUnarchiveSwipeEnabled() {
        return prefs().getBoolean(KEY_DISABLE_UNARCHIVE_SWIPE, true);
    }
    public static void setDisableUnarchiveSwipeEnabled(boolean value) {
        prefs().edit().putBoolean(KEY_DISABLE_UNARCHIVE_SWIPE, value).apply();
    }
}
