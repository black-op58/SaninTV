package com.sanin.tv.settings.saving

import android.graphics.Color
import kotlin.reflect.KClass

data class Pref<T : Any>(val location: Location, val type: KClass<T>, val default: T)

enum class Location { Player, Reader, General }

enum class PrefName(val data: Pref<out Any>) {

    // ── Player ────────────────────────────────────────────────────────────────
    TextviewSubtitles(Pref(Location.Player, Boolean::class, false)),
    SubLanguage(Pref(Location.Player, Int::class, 9)),
    PrimaryColor(Pref(Location.Player, Int::class, Color.WHITE)),
    SecondaryColor(Pref(Location.Player, Int::class, Color.BLACK)),
    Outline(Pref(Location.Player, Int::class, 0)),
    SubBackground(Pref(Location.Player, Int::class, Color.TRANSPARENT)),
    SubWindow(Pref(Location.Player, Int::class, Color.TRANSPARENT)),
    SubAlpha(Pref(Location.Player, Float::class, 1f)),
    SubStroke(Pref(Location.Player, Float::class, 8f)),
    SubBottomMargin(Pref(Location.Player, Float::class, 1f)),
    Font(Pref(Location.Player, Int::class, 0)),
    FontSize(Pref(Location.Player, Int::class, 20)),
    Locale(Pref(Location.Player, Int::class, 2)),
    TimeStampsEnabled(Pref(Location.Player, Boolean::class, true)),
    AutoHideTimeStamps(Pref(Location.Player, Boolean::class, true)),
    UseProxyForTimeStamps(Pref(Location.Player, Boolean::class, false)),
    ShowTimeStampButton(Pref(Location.Player, Boolean::class, true)),
    AutoSkipOPED(Pref(Location.Player, Boolean::class, false)),
    AutoSkipRecap(Pref(Location.Player, Boolean::class, false)),
    AutoPlay(Pref(Location.Player, Boolean::class, true)),
    AutoSkipFiller(Pref(Location.Player, Boolean::class, false)),
    AskIndividualPlayer(Pref(Location.Player, Boolean::class, true)),
    ChapterZeroPlayer(Pref(Location.Player, Boolean::class, true)),
    UpdateForHPlayer(Pref(Location.Player, Boolean::class, false)),
    WatchPercentage(Pref(Location.Player, Float::class, 0.8f)),
    AlwaysContinue(Pref(Location.Player, Boolean::class, true)),
    FocusPause(Pref(Location.Player, Boolean::class, true)),
    Gestures(Pref(Location.Player, Boolean::class, true)),
    DoubleTap(Pref(Location.Player, Boolean::class, true)),
    FastForward(Pref(Location.Player, Boolean::class, true)),
    SeekTime(Pref(Location.Player, Int::class, 10)),
    SkipTime(Pref(Location.Player, Int::class, 85)),
    Cast(Pref(Location.Player, Boolean::class, true)),
    UseInternalCast(Pref(Location.Player, Boolean::class, false)),
    Pip(Pref(Location.Player, Boolean::class, true)),
    RotationPlayer(Pref(Location.Player, Boolean::class, true)),
    UseAdditionalCodec(Pref(Location.Player, Boolean::class, false)),

    // ── Reader ────────────────────────────────────────────────────────────────
    ShowSource(Pref(Location.Reader, Boolean::class, true)),
    ShowSystemBars(Pref(Location.Reader, Boolean::class, false)),
    AutoDetectWebtoon(Pref(Location.Reader, Boolean::class, true)),
    AskIndividualReader(Pref(Location.Reader, Boolean::class, true)),
    ChapterZeroReader(Pref(Location.Reader, Boolean::class, true)),
    UpdateForHReader(Pref(Location.Reader, Boolean::class, false)),
    Direction(Pref(Location.Reader, Int::class, 0)),
    LayoutReader(Pref(Location.Reader, Int::class, 2)),
    DualPageModeReader(Pref(Location.Reader, Int::class, 1)),
    OverScrollMode(Pref(Location.Reader, Boolean::class, true)),
    TrueColors(Pref(Location.Reader, Boolean::class, false)),
    Rotation(Pref(Location.Reader, Boolean::class, true)),
    Padding(Pref(Location.Reader, Boolean::class, true)),
    HideScrollBar(Pref(Location.Reader, Boolean::class, false)),
    HidePageNumbers(Pref(Location.Reader, Boolean::class, false)),
    HorizontalScrollBar(Pref(Location.Reader, Boolean::class, true)),
    KeepScreenOn(Pref(Location.Reader, Boolean::class, false)),
    VolumeButtonsReader(Pref(Location.Reader, Boolean::class, false)),
    WrapImages(Pref(Location.Reader, Boolean::class, false)),
    LongClickImage(Pref(Location.Reader, Boolean::class, true)),
    CropBorders(Pref(Location.Reader, Boolean::class, false)),
    CropBorderThreshold(Pref(Location.Reader, Int::class, 10)),
    DataSaverMode(Pref(Location.Reader, Int::class, 0)),
    DataSaverImageQuality(Pref(Location.Reader, Int::class, 80)),
    DataSaverImageFormatJpeg(Pref(Location.Reader, Boolean::class, false)),
    DataSaverIgnoreJpeg(Pref(Location.Reader, Boolean::class, false)),
    DataSaverIgnoreGif(Pref(Location.Reader, Boolean::class, true)),
    DataSaverServer(Pref(Location.Reader, String::class, "https://bandwidth-hero-proxy-nu-nine.vercel.app/")),
    DataSaverColorBW(Pref(Location.Reader, Boolean::class, false)),
    CurrentThemeName(Pref(Location.Reader, String::class, "Default")),
    LayoutNovel(Pref(Location.Reader, Int::class, 0)),
    DualPageModeNovel(Pref(Location.Reader, Int::class, 1)),
    LineHeight(Pref(Location.Reader, Float::class, 1.4f)),
    Margin(Pref(Location.Reader, Float::class, 0.06f)),
    Justify(Pref(Location.Reader, Boolean::class, true)),
    Hyphenation(Pref(Location.Reader, Boolean::class, true)),

    // ── OLED Background Mode ──────────────────────────────────────────────────
    // 0 = Off          — normal theme background
    // 1 = Pure AMOLED  — pure black (#000000) background, saves OLED pixels
    // 2 = Glow Spots   — black + intense radial glow orbs (top-center, top-right, bottom-left)
    // 3 = Gradient     — black + linear gradient in the theme primary color
    OledMode(Pref(Location.General, Int::class, 0)),

    // ── Gradient Direction ────────────────────────────────────────────────────
    // 0 = Top → Bottom  (default)
    // 1 = Bottom → Top
    // 2 = Left → Right
    // 3 = Right → Left
    GradientDirection(Pref(Location.General, Int::class, 0)),

    // ── UI / General ──────────────────────────────────────────────────────────
    BannerAnimations(Pref(Location.General, Boolean::class, true)),
    LayoutAnimations(Pref(Location.General, Boolean::class, true)),
    Emoji(Pref(Location.General, Boolean::class, true)),
    BlurBanners(Pref(Location.General, Boolean::class, true)),
    SmallView(Pref(Location.General, Boolean::class, false)),
    ImmersiveMode(Pref(Location.General, Boolean::class, false)),
    AnimationSpeed(Pref(Location.General, Float::class, 1.0f)),
    ShowNotificationRedDot(Pref(Location.General, Boolean::class, true)),
    RescueMode(Pref(Location.General, Boolean::class, false)),
    AnilistUserId(Pref(Location.General, String::class, "")),
    CheckUpdate(Pref(Location.General, Boolean::class, true)),
    RefreshStatus(Pref(Location.General, Boolean::class, false)),

    // Home layout — stored as custom JSON lists via PrefManager.setCustomVal /
    // getNullableCustomVal; getVal falls back to the typed default here.
    HomeLayout(Pref(Location.General, Boolean::class, true)),
    HomeLayoutOrder(Pref(Location.General, Int::class, 0)),

    // ── Home Banner Mode ──────────────────────────────────────────────────────
    // 0 = CAROUSEL   — auto-cycling ViewPager of the 15 most popular anime
    // 1 = PROFILE    — the user's AniList profile banner image
    // 2 = NAVIGATING — crossfades to the focused card's banner (last-watched on launch)
    // 3 = OFF        — no banner at all; cards start at the very top
    HomeBannerMode(Pref(Location.General, Int::class, 2)),

    // ── Logo Art cache ────────────────────────────────────────────────────────
    // TmdbApiKey removed — key is now held server-side, no user input required.
    CacheStorageUri(Pref(Location.General, String::class, "")),

    // ── Continue Watching ─────────────────────────────────────────────────────
    SourceMemoryExpiryHours(Pref(Location.Player, Int::class, 24)),
    ContinueWatchingShowScreenshot(Pref(Location.Player, Boolean::class, false)),

    // ── Content ───────────────────────────────────────────────────────────────
    SmartSourcePersistence(Pref(Location.General, Boolean::class, false)),
    PreferDub(Pref(Location.General, Boolean::class, false)),

    // ── Advanced Video / Player ───────────────────────────────────────────────
    // QualityProfile: 0=Auto, 1=Fast, 2=High, 3=Custom
    QualityProfile(Pref(Location.Player, Int::class, 0)),
    // AutoHideTimeout: 2-10 s
    AutoHideTimeout(Pref(Location.Player, Int::class, 5)),
    // BufferSize: 16-128 MB
    BufferSize(Pref(Location.Player, Int::class, 32)),
    HardwareDecoding(Pref(Location.Player, Boolean::class, true)),
    // VideoOutput: 0=gpu, 1=opengl, 2=vulkan
    VideoOutput(Pref(Location.Player, Int::class, 0)),
    // GpuContext: 0=auto, 1=opengl, 2=vulkan
    GpuContext(Pref(Location.Player, Int::class, 0)),
    Debanding(Pref(Location.Player, Boolean::class, false)),
    Interpolation(Pref(Location.Player, Boolean::class, false)),
    // UpscalingAlgorithm: 0=Bilinear, 1=Bicubic, 2=Lanczos, 3=NNEDI3
    UpscalingAlgorithm(Pref(Location.Player, Int::class, 0)),
    RawConfiguration(Pref(Location.Player, String::class, "")),

    // ── Theme extras ──────────────────────────────────────────────────────────
    // AccentColor: 0 = use theme default (no override)
    AccentColor(Pref(Location.General, Int::class, 0)),
    SwapColors(Pref(Location.General, Boolean::class, false)),
    UseMaterial3(Pref(Location.General, Boolean::class, true)),
    // BlendLevel: 0-20
    BlendLevel(Pref(Location.General, Int::class, 10)),

    // ── UI / Cards ────────────────────────────────────────────────────────────
    // CardStyle: 0=rounded, 1=minimal, 2=classic, 3=coverOnly, 4=liquidGlass, 5=neon, 6=compact
    CardStyle(Pref(Location.General, Int::class, 0)),
    // SpotlightCardStyle: 0=default, 1=minimal, 2=large
    SpotlightCardStyle(Pref(Location.General, Int::class, 0)),
    // EpisodeViewMode: 0=list, 1=grid, 2=compact
    EpisodeViewMode(Pref(Location.General, Int::class, 0)),
    // UIScale: 0.5-2.0
    UIScale(Pref(Location.General, Float::class, 1.0f)),
    AnimationsEnabled(Pref(Location.General, Boolean::class, true)),
    TrendingScroller(Pref(Location.General, Boolean::class, true)),
    // OverallSpeed: 1-5
    OverallSpeed(Pref(Location.General, Float::class, 1.0f)),
    // BlurRadius: 1-50 px
    BlurRadius(Pref(Location.General, Int::class, 10)),
    // Sampling: 1-8
    Sampling(Pref(Location.General, Int::class, 1)),

    // ── Focus Effects ─────────────────────────────────────────────────────────
    // 0=Glow, 1=Breathing, 2=Pulse, 3=Shaking, 4=None
    FocusEffect(Pref(Location.General, Int::class, 0)),

    // ── Sync / Progress ───────────────────────────────────────────────────────
    AutoSyncAniList(Pref(Location.General, Boolean::class, true)),
    UpdateProgressAutomatically(Pref(Location.General, Boolean::class, true)),
    UpdateProgressForChapters(Pref(Location.General, Boolean::class, false)),
    UpdateProgressForHentai(Pref(Location.General, Boolean::class, false)),

    // ── Home layout toggles ───────────────────────────────────────────────────
    ShowContinueWatching(Pref(Location.General, Boolean::class, true)),
    ShowPlanned(Pref(Location.General, Boolean::class, true)),
    ShowRecommendations(Pref(Location.General, Boolean::class, true)),
    ShowTrending(Pref(Location.General, Boolean::class, true)),
    ShowPopular(Pref(Location.General, Boolean::class, true)),
    ShowRecent(Pref(Location.General, Boolean::class, true)),

    // ── General / Advanced ────────────────────────────────────────────────────
    // DefaultStartUpTab: 0=home, 1=anime, 2=search, 3=library, 4=profile
    DefaultStartUpTab(Pref(Location.General, Int::class, 0)),
    // SelectedDNS: 0=CLOUDFLARE, 1=GOOGLE, 2=QUAD9, 3=ADGUARD, 4=CUSTOM
    SelectedDNS(Pref(Location.General, Int::class, 0)),
    CustomDnsIP(Pref(Location.General, String::class, "")),
    DataSaverEnabled(Pref(Location.General, Boolean::class, false)),
    AutoUpdateExtensions(Pref(Location.General, Boolean::class, true)),
    Incognito(Pref(Location.General, Boolean::class, false)),
    BrightnessGestures(Pref(Location.Player, Boolean::class, true)),
    // CardOrientation: 0=landscape, 1=portrait (default)
    CardOrientation(Pref(Location.General, Int::class, 1)),
    // CardImageType: 0=cover art (default), 1=banner art
    CardImageType(Pref(Location.General, Int::class, 0)),
    // CardRoundness: corner radius in dp (0=none, 8=small, 16=medium, 24=large, 50=pill)
    CardRoundness(Pref(Location.General, Int::class, 16)),
    // Card indicator toggles
    ShowNewEpisodeBadge(Pref(Location.General, Boolean::class, true)),
    ShowReleasingIndicator(Pref(Location.General, Boolean::class, true)),
}
