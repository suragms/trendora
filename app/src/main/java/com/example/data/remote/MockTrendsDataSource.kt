package com.example.data.remote

import com.example.domain.model.*

object MockTrendsDataSource {

    fun getInitialTrends(): List<TrendItem> {
        return listOf(
            TrendItem(
                id = "trend_ai_gen",
                title = "Artificial Intelligence",
                category = TrendCategory.AI,
                score = TrendScore.calculate(
                    searchGrowth = 99,
                    socialMentions = 98,
                    engagement = 97,
                    newsCoverage = 96,
                    growthVelocity = 99
                ),
                growthPercentage = 142,
                discussionsCount = "+185K discussions",
                searchVolume = "1.4M searches",
                timeAgo = "12m ago",
                country = Country.GLOBAL,
                chartData = listOf(45f, 52f, 60f, 68f, 75f, 88f, 94f, 98f),
                historyToday = listOf(
                    TrendHistoryPoint("00:00", 65f),
                    TrendHistoryPoint("04:00", 70f),
                    TrendHistoryPoint("08:00", 82f),
                    TrendHistoryPoint("12:00", 91f),
                    TrendHistoryPoint("16:00", 95f),
                    TrendHistoryPoint("20:00", 98f)
                ),
                history7Days = listOf(
                    TrendHistoryPoint("Mon", 72f),
                    TrendHistoryPoint("Tue", 78f),
                    TrendHistoryPoint("Wed", 80f),
                    TrendHistoryPoint("Thu", 85f),
                    TrendHistoryPoint("Fri", 90f),
                    TrendHistoryPoint("Sat", 94f),
                    TrendHistoryPoint("Sun", 98f)
                ),
                history30Days = listOf(
                    TrendHistoryPoint("Week 1", 60f),
                    TrendHistoryPoint("Week 2", 72f),
                    TrendHistoryPoint("Week 3", 84f),
                    TrendHistoryPoint("Week 4", 98f)
                ),
                sourceIcons = listOf("X", "Reddit", "Google", "YouTube"),
                isTrendingUp = true,
                isBreaking = true,
                aiAnalysis = AIAnalysis(
                    summary = "Artificial Intelligence is rapidly surging today due to major multi-agent breakthroughs, flagship model releases, open-weight reasoning announcements, and global developer discussions.",
                    whyTrending = "A synchronized wave of cutting-edge multimodal models, new autonomous reasoning frameworks, and major enterprise integrations have ignited unprecedented social and technical interest worldwide.",
                    sentiment = SentimentBreakdown(
                        positivePercent = 78,
                        neutralPercent = 14,
                        negativePercent = 8,
                        summary = "Strongly positive sentiment driven by productivity leaps and creative agent breakthroughs, with minor debates on compute energy."
                    ),
                    expectedGrowthPercent = 94,
                    viralProbabilityPercent = 92,
                    aiConfidencePercent = 96,
                    growthTrajectory = "Rapid Exponential",
                    relatedTopics = listOf("Reasoning Models", "Autonomous Agents", "Robotics", "Neural Hardware", "Transformer Compute"),
                    timeline = listOf(
                        "02:00 UTC: Initial research benchmarks published on ArXiv",
                        "06:30 UTC: Developer demos go viral on X & GitHub trending",
                        "10:15 UTC: Tech publications release deep-dive benchmarks",
                        "Now: Sustained peak engagement across global engineering hubs"
                    )
                ),
                discussions = listOf(
                    DiscussionItem(
                        id = "d1",
                        author = "Sarah Chen",
                        handle = "@sarahc_ai",
                        platform = "X / Twitter",
                        content = "The leap in multi-step reasoning models this morning is mind-bending. Benchmark scores are surpassing PhD-level logic tests with sub-second latency.",
                        timeAgo = "18m ago",
                        likesCount = "14.2K",
                        commentsCount = "1.8K",
                        sentiment = "Positive"
                    ),
                    DiscussionItem(
                        id = "d2",
                        author = "r/MachineLearning",
                        handle = "u/deep_coder",
                        platform = "Reddit",
                        content = "Benchmark comparison megathread: Detailed memory footprint analysis and token throughput across all new lightweight distilled weights.",
                        timeAgo = "45m ago",
                        likesCount = "8.9K",
                        commentsCount = "920",
                        sentiment = "Positive"
                    ),
                    DiscussionItem(
                        id = "d3",
                        author = "TechCrunch Daily",
                        handle = "@techcrunch",
                        platform = "TechNews",
                        content = "Venture investments in autonomous enterprise workflows jump 300% quarter-over-quarter as frontier models standardize.",
                        timeAgo = "1h ago",
                        likesCount = "5.3K",
                        commentsCount = "410",
                        sentiment = "Neutral"
                    )
                ),
                relatedNews = listOf(
                    BreakingNewsItem(
                        id = "n1",
                        headline = "Next-Gen Autonomous Multi-Agent Workflows Set New Benchmark in Software Engineering",
                        source = "Wired",
                        timeAgo = "25m ago",
                        imageUrl = "https://images.unsplash.com/photo-1677442136019-21780ecad995?w=800&q=80",
                        category = TrendCategory.AI,
                        trendingScore = 98,
                        summary = "Autonomous agent clusters complete full-stack refactors in minutes, unlocking massive productivity gains for engineering teams."
                    )
                )
            ),
            TrendItem(
                id = "trend_iphone_18",
                title = "iPhone 18 Ultra Titanium",
                category = TrendCategory.TECH,
                score = TrendScore.calculate(
                    searchGrowth = 94,
                    socialMentions = 93,
                    engagement = 91,
                    newsCoverage = 89,
                    growthVelocity = 93
                ),
                growthPercentage = 115,
                discussionsCount = "+140K discussions",
                searchVolume = "980K searches",
                timeAgo = "24m ago",
                country = Country.USA,
                chartData = listOf(35f, 48f, 55f, 68f, 79f, 85f, 91f, 92f),
                historyToday = listOf(
                    TrendHistoryPoint("00:00", 50f),
                    TrendHistoryPoint("06:00", 65f),
                    TrendHistoryPoint("12:00", 82f),
                    TrendHistoryPoint("18:00", 92f)
                ),
                history7Days = listOf(
                    TrendHistoryPoint("Mon", 60f),
                    TrendHistoryPoint("Wed", 75f),
                    TrendHistoryPoint("Fri", 88f),
                    TrendHistoryPoint("Sun", 92f)
                ),
                history30Days = listOf(
                    TrendHistoryPoint("W1", 45f),
                    TrendHistoryPoint("W2", 60f),
                    TrendHistoryPoint("W3", 80f),
                    TrendHistoryPoint("W4", 92f)
                ),
                sourceIcons = listOf("X", "YouTube", "Reddit", "Google"),
                isTrendingUp = true,
                isBreaking = false,
                aiAnalysis = AIAnalysis(
                    summary = "Leaked CAD schematics and manufacturing supply chain reports reveal an under-display front camera matrix, solid-state silicon-carbon battery, and periscope sensor upgrades.",
                    whyTrending = "Exclusive supply-chain leaks and 3D renders from leading hardware analysts revealed a zero-bezel chassis and quantum dot display panel.",
                    sentiment = SentimentBreakdown(
                        positivePercent = 71,
                        neutralPercent = 20,
                        negativePercent = 9,
                        summary = "Widespread excitement over physical design innovation and battery density gains."
                    ),
                    expectedGrowthPercent = 89,
                    viralProbabilityPercent = 86,
                    aiConfidencePercent = 91,
                    growthTrajectory = "Sustained Peak",
                    relatedTopics = listOf("Titanium Design", "Silicon Battery", "Under-Display Sensor", "Spatial Camera", "A20 Pro"),
                    timeline = listOf(
                        "04:00 UTC: Schematics posted to social media forums",
                        "08:00 UTC: Prominent tech YouTubers release 3D visual breakdown",
                        "Now: Heavy trending topic in North America and Asia"
                    )
                ),
                discussions = listOf(
                    DiscussionItem(
                        id = "d4",
                        author = "Marques K.",
                        handle = "@mkbhd_insight",
                        platform = "YouTube",
                        content = "If these zero-bezel titanium tolerances are real, this is the biggest leap in smartphone industrial design since 2017.",
                        timeAgo = "32m ago",
                        likesCount = "28.5K",
                        commentsCount = "3.2K",
                        sentiment = "Positive"
                    )
                ),
                relatedNews = listOf(
                    BreakingNewsItem(
                        id = "n2",
                        headline = "Supply Chain Leaks Confirm Groundbreaking Silicon-Carbon Battery for Next Flagship",
                        source = "Bloomberg Tech",
                        timeAgo = "40m ago",
                        imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80",
                        category = TrendCategory.TECH,
                        trendingScore = 92
                    )
                )
            ),
            TrendItem(
                id = "trend_kerala_tech",
                title = "Kerala Technology & Startup Hub",
                category = TrendCategory.TECH,
                score = TrendScore.calculate(
                    searchGrowth = 88,
                    socialMentions = 86,
                    engagement = 87,
                    newsCoverage = 85,
                    growthVelocity = 89
                ),
                growthPercentage = 98,
                discussionsCount = "+76K discussions",
                searchVolume = "420K searches",
                timeAgo = "38m ago",
                country = Country.INDIA,
                chartData = listOf(28f, 36f, 48f, 58f, 72f, 80f, 84f, 87f),
                historyToday = listOf(
                    TrendHistoryPoint("00:00", 40f),
                    TrendHistoryPoint("08:00", 62f),
                    TrendHistoryPoint("14:00", 78f),
                    TrendHistoryPoint("20:00", 87f)
                ),
                history7Days = listOf(
                    TrendHistoryPoint("Mon", 50f),
                    TrendHistoryPoint("Wed", 68f),
                    TrendHistoryPoint("Fri", 79f),
                    TrendHistoryPoint("Sun", 87f)
                ),
                history30Days = listOf(
                    TrendHistoryPoint("W1", 35f),
                    TrendHistoryPoint("W2", 52f),
                    TrendHistoryPoint("W3", 70f),
                    TrendHistoryPoint("W4", 87f)
                ),
                sourceIcons = listOf("X", "Google", "LinkedIn", "YouTube"),
                isTrendingUp = true,
                isBreaking = false,
                aiAnalysis = AIAnalysis(
                    summary = "Kerala's Silicon Coast and Technopark expansion surges with record unicorn emergence, AI park inaugurations, and deep-tech robotics investments.",
                    whyTrending = "Global venture capital inflows into Kochi & Trivandrum innovation clusters hit a record high following major international cloud partnerships and AI campus launches.",
                    sentiment = SentimentBreakdown(
                        positivePercent = 86,
                        neutralPercent = 11,
                        negativePercent = 3,
                        summary = "Overwhelmingly optimistic about southern India's engineering talent pool and sustainable tech infrastructure."
                    ),
                    expectedGrowthPercent = 88,
                    viralProbabilityPercent = 82,
                    aiConfidencePercent = 90,
                    growthTrajectory = "Steady Climb",
                    relatedTopics = listOf("Technopark Kochi", "Startup Mission", "AI Corridor", "Semiconductor Fab", "Deep Tech"),
                    timeline = listOf(
                        "05:00 UTC: Government and global tech CEOs sign MoUs",
                        "09:00 UTC: Startup pitch competition highlights $200M funding rounds",
                        "Now: Trending across Indian developer communities"
                    )
                ),
                discussions = listOf(
                    DiscussionItem(
                        id = "d5",
                        author = "Kochi Tech Forum",
                        handle = "@tech_kerala",
                        platform = "X / Twitter",
                        content = "The new AI Innovation Corridor at Infopark is officially live! Over 60 deep-tech startups already setting up operations.",
                        timeAgo = "1h ago",
                        likesCount = "6.1K",
                        commentsCount = "450",
                        sentiment = "Positive"
                    )
                ),
                relatedNews = listOf(
                    BreakingNewsItem(
                        id = "n3",
                        headline = "Kerala Tech Corridor Attracts Global Tech Giants with New AI Supercluster",
                        source = "The Hindu Tech",
                        timeAgo = "1h ago",
                        imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&q=80",
                        category = TrendCategory.TECH,
                        trendingScore = 87
                    )
                )
            ),
            TrendItem(
                id = "trend_fusion_energy",
                title = "Clean Fusion Energy Net Gain",
                category = TrendCategory.BUSINESS,
                score = TrendScore.calculate(
                    searchGrowth = 95,
                    socialMentions = 92,
                    engagement = 93,
                    newsCoverage = 94,
                    growthVelocity = 92
                ),
                growthPercentage = 160,
                discussionsCount = "+110K discussions",
                searchVolume = "810K searches",
                timeAgo = "45m ago",
                country = Country.GLOBAL,
                chartData = listOf(20f, 32f, 44f, 62f, 78f, 86f, 91f, 93f),
                historyToday = listOf(
                    TrendHistoryPoint("00:00", 30f),
                    TrendHistoryPoint("06:00", 55f),
                    TrendHistoryPoint("12:00", 80f),
                    TrendHistoryPoint("18:00", 93f)
                ),
                history7Days = listOf(
                    TrendHistoryPoint("Mon", 40f),
                    TrendHistoryPoint("Wed", 60f),
                    TrendHistoryPoint("Fri", 82f),
                    TrendHistoryPoint("Sun", 93f)
                ),
                history30Days = listOf(
                    TrendHistoryPoint("W1", 25f),
                    TrendHistoryPoint("W2", 45f),
                    TrendHistoryPoint("W3", 68f),
                    TrendHistoryPoint("W4", 93f)
                ),
                sourceIcons = listOf("X", "Reddit", "Nature", "YouTube"),
                isTrendingUp = true,
                isBreaking = true,
                aiAnalysis = AIAnalysis(
                    summary = "Magnetically confined tokamak test achieves continuous 120-second plasma stabilization with 2.8x Q-factor net energy gain.",
                    whyTrending = "Peer-reviewed papers confirmed commercial-scale fusion milestones, triggering investments from sovereign wealth funds and renewable energy giants.",
                    sentiment = SentimentBreakdown(
                        positivePercent = 91,
                        neutralPercent = 7,
                        negativePercent = 2,
                        summary = "Extraordinary global enthusiasm regarding unlimited clean base-load power."
                    ),
                    expectedGrowthPercent = 95,
                    viralProbabilityPercent = 90,
                    aiConfidencePercent = 94,
                    growthTrajectory = "Rapid Exponential",
                    relatedTopics = listOf("Tokamak Plasma", "Clean Power", "Superconductors", "Zero Emission", "Grid Scale"),
                    timeline = listOf(
                        "01:00 UTC: Nature Physics publishes peer-reviewed verification",
                        "07:00 UTC: Energy ministers brief press",
                        "Now: Global viral discussion across science & financial channels"
                    )
                ),
                discussions = listOf(
                    DiscussionItem(
                        id = "d6",
                        author = "Physics Today",
                        handle = "@physicstoday",
                        platform = "X / Twitter",
                        content = "High-temperature superconducting magnets held continuous burning plasma for over two full minutes with Q>2.5. A pivotal turning point in human physics.",
                        timeAgo = "50m ago",
                        likesCount = "22.4K",
                        commentsCount = "1.5K",
                        sentiment = "Positive"
                    )
                ),
                relatedNews = listOf(
                    BreakingNewsItem(
                        id = "n4",
                        headline = "Historic Fusion Plasma Test Delivers Sustained 2.8x Net Energy Milestone",
                        source = "Reuters Science",
                        timeAgo = "55m ago",
                        imageUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=800&q=80",
                        category = TrendCategory.BUSINESS,
                        trendingScore = 93
                    )
                )
            ),
            TrendItem(
                id = "trend_gta6_trailer",
                title = "Grand Theft Auto VI Gameplay",
                category = TrendCategory.GAMING,
                score = TrendScore.calculate(
                    searchGrowth = 98,
                    socialMentions = 96,
                    engagement = 95,
                    newsCoverage = 91,
                    growthVelocity = 95
                ),
                growthPercentage = 210,
                discussionsCount = "+290K discussions",
                searchVolume = "2.6M searches",
                timeAgo = "1h ago",
                country = Country.GLOBAL,
                chartData = listOf(50f, 62f, 75f, 84f, 90f, 93f, 95f, 95f),
                historyToday = listOf(
                    TrendHistoryPoint("00:00", 70f),
                    TrendHistoryPoint("08:00", 85f),
                    TrendHistoryPoint("16:00", 95f)
                ),
                history7Days = listOf(
                    TrendHistoryPoint("Mon", 80f),
                    TrendHistoryPoint("Wed", 88f),
                    TrendHistoryPoint("Fri", 93f),
                    TrendHistoryPoint("Sun", 95f)
                ),
                history30Days = listOf(
                    TrendHistoryPoint("W1", 70f),
                    TrendHistoryPoint("W2", 82f),
                    TrendHistoryPoint("W3", 90f),
                    TrendHistoryPoint("W4", 95f)
                ),
                sourceIcons = listOf("YouTube", "X", "Reddit", "Twitch"),
                isTrendingUp = true,
                isBreaking = false,
                aiAnalysis = AIAnalysis(
                    summary = "Official 4K 60FPS in-engine ray-tracing footage showcases dynamic volumetric weather, photorealistic NPC neural crowd behaviors, and Vice City map scale.",
                    whyTrending = "Rockstar Games dropped a surprise 8-minute deep-dive gameplay preview, instantly breaking YouTube 24-hour premiere records.",
                    sentiment = SentimentBreakdown(
                        positivePercent = 89,
                        neutralPercent = 8,
                        negativePercent = 3,
                        summary = "Massive excitement across global gaming communities with praise for visual realism."
                    ),
                    expectedGrowthPercent = 96,
                    viralProbabilityPercent = 98,
                    aiConfidencePercent = 97,
                    growthTrajectory = "Rapid Exponential",
                    relatedTopics = listOf("Vice City Map", "Ray Tracing", "Rockstar Games", "PS5 Pro Mode", "Open World AI"),
                    timeline = listOf(
                        "14:00 UTC: YouTube Premiere goes live with 4M concurrent viewers",
                        "15:00 UTC: Reddit r/gaming breaks all-time hourly comment record"
                    )
                ),
                discussions = listOf(
                    DiscussionItem(
                        id = "d7",
                        author = "IGN Gaming",
                        handle = "@IGN",
                        platform = "YouTube",
                        content = "The physics engine in Vice City water dynamics and interior building density look generational.",
                        timeAgo = "1h ago",
                        likesCount = "45K",
                        commentsCount = "4.9K",
                        sentiment = "Positive"
                    )
                ),
                relatedNews = listOf(
                    BreakingNewsItem(
                        id = "n5",
                        headline = "New Gameplay Footage Shakes Gaming World with Unrivaled Photorealism",
                        source = "Eurogamer",
                        timeAgo = "1h ago",
                        imageUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&q=80",
                        category = TrendCategory.GAMING,
                        trendingScore = 95
                    )
                )
            ),
            TrendItem(
                id = "trend_premier_league",
                title = "Premier League Title Race Derby",
                category = TrendCategory.SPORTS,
                score = TrendScore.calculate(
                    searchGrowth = 86,
                    socialMentions = 85,
                    engagement = 84,
                    newsCoverage = 82,
                    growthVelocity = 84
                ),
                growthPercentage = 75,
                discussionsCount = "+92K discussions",
                searchVolume = "640K searches",
                timeAgo = "1h ago",
                country = Country.UK,
                chartData = listOf(40f, 48f, 58f, 66f, 74f, 80f, 83f, 84f),
                historyToday = listOf(
                    TrendHistoryPoint("00:00", 50f),
                    TrendHistoryPoint("08:00", 65f),
                    TrendHistoryPoint("16:00", 84f)
                ),
                history7Days = listOf(
                    TrendHistoryPoint("Mon", 60f),
                    TrendHistoryPoint("Wed", 70f),
                    TrendHistoryPoint("Fri", 78f),
                    TrendHistoryPoint("Sun", 84f)
                ),
                history30Days = listOf(
                    TrendHistoryPoint("W1", 55f),
                    TrendHistoryPoint("W2", 65f),
                    TrendHistoryPoint("W3", 75f),
                    TrendHistoryPoint("W4", 84f)
                ),
                sourceIcons = listOf("X", "BBC", "Reddit", "SkySports"),
                isTrendingUp = true,
                isBreaking = false,
                aiAnalysis = AIAnalysis(
                    summary = "Stunning 94th-minute bicycle kick equalizer in London Derby reshuffles the top 3 standings with only 4 matchweeks remaining.",
                    whyTrending = "Dramatic stoppage-time winner and controversial VAR review in title decider generated immense debates across UK and worldwide football fandom.",
                    sentiment = SentimentBreakdown(
                        positivePercent = 65,
                        neutralPercent = 20,
                        negativePercent = 15,
                        summary = "Passionate tribal debate between supporter bases with high matchday engagement."
                    ),
                    expectedGrowthPercent = 78,
                    viralProbabilityPercent = 80,
                    aiConfidencePercent = 88,
                    growthTrajectory = "Sustained Peak",
                    relatedTopics = listOf("London Derby", "VAR Decision", "Golden Boot", "Champions League Qualification", "Arsenal ManCity"),
                    timeline = listOf(
                        "16:45 UTC: 94th-minute goal scored",
                        "17:00 UTC: Post-match manager interviews broadcast live"
                    )
                ),
                discussions = listOf(
                    DiscussionItem(
                        id = "d8",
                        author = "Gary Lineker",
                        handle = "@GaryLineker",
                        platform = "X / Twitter",
                        content = "What an absolute spectacle of a match. Premier League football at its thrilling, unpredictable finest!",
                        timeAgo = "1h ago",
                        likesCount = "18.3K",
                        commentsCount = "1.2K",
                        sentiment = "Positive"
                    )
                ),
                relatedNews = listOf(
                    BreakingNewsItem(
                        id = "n6",
                        headline = "Stoppage-Time Thriller Leaves Title Race on Knife-Edge in Front of 60,000 Fans",
                        source = "Sky Sports",
                        timeAgo = "1h ago",
                        imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800&q=80",
                        category = TrendCategory.SPORTS,
                        trendingScore = 84
                    )
                )
            ),
            TrendItem(
                id = "trend_mars_base",
                title = "Artemis & Mars Habitat Modules",
                category = TrendCategory.WORLD,
                score = TrendScore.calculate(
                    searchGrowth = 89,
                    socialMentions = 87,
                    engagement = 88,
                    newsCoverage = 90,
                    growthVelocity = 87
                ),
                growthPercentage = 84,
                discussionsCount = "+68K discussions",
                searchVolume = "510K searches",
                timeAgo = "2h ago",
                country = Country.GLOBAL,
                chartData = listOf(30f, 42f, 55f, 68f, 76f, 82f, 86f, 88f),
                historyToday = listOf(
                    TrendHistoryPoint("00:00", 45f),
                    TrendHistoryPoint("08:00", 68f),
                    TrendHistoryPoint("16:00", 88f)
                ),
                history7Days = listOf(
                    TrendHistoryPoint("Mon", 52f),
                    TrendHistoryPoint("Wed", 68f),
                    TrendHistoryPoint("Fri", 80f),
                    TrendHistoryPoint("Sun", 88f)
                ),
                history30Days = listOf(
                    TrendHistoryPoint("W1", 40f),
                    TrendHistoryPoint("W2", 58f),
                    TrendHistoryPoint("W3", 74f),
                    TrendHistoryPoint("W4", 88f)
                ),
                sourceIcons = listOf("X", "NASA", "Reddit", "YouTube"),
                isTrendingUp = true,
                isBreaking = false,
                aiAnalysis = AIAnalysis(
                    summary = "Autonomous 3D-printed regolith habitat completed 365-day simulated deep-space pressurization test with 100% water and oxygen recycling efficiency.",
                    whyTrending = "Space agencies and aerospace contractors unveiled the completed full-scale Martian biosphere dome designed for 8 astronauts.",
                    sentiment = SentimentBreakdown(
                        positivePercent = 88,
                        neutralPercent = 10,
                        negativePercent = 2,
                        summary = "Global wonder and scientific acclaim for interplanetary exploration roadmap."
                    ),
                    expectedGrowthPercent = 85,
                    viralProbabilityPercent = 84,
                    aiConfidencePercent = 92,
                    growthTrajectory = "Steady Climb",
                    relatedTopics = listOf("Artemis V", "Lunar Gateway", "Regolith 3D Print", "Biosphere Recycling", "Starship Heavy"),
                    timeline = listOf(
                        "08:00 UTC: NASA and ESA live-stream habitat interior tour",
                        "12:00 UTC: Astronaut crew completes 1-year isolation simulation"
                    )
                ),
                discussions = listOf(
                    DiscussionItem(
                        id = "d9",
                        author = "SpaceX Updates",
                        handle = "@SpaceX_Daily",
                        platform = "X / Twitter",
                        content = "The closed-loop environmental control inside the new Martian dome achieved 98.6% moisture reclamation over 12 consecutive months.",
                        timeAgo = "2h ago",
                        likesCount = "19K",
                        commentsCount = "890",
                        sentiment = "Positive"
                    )
                ),
                relatedNews = listOf(
                    BreakingNewsItem(
                        id = "n7",
                        headline = "Crew Emerges Victorious from 365-Day Sealed Martian Habitat Simulation",
                        source = "BBC World Science",
                        timeAgo = "2h ago",
                        imageUrl = "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=800&q=80",
                        category = TrendCategory.WORLD,
                        trendingScore = 88
                    )
                )
            ),
            TrendItem(
                id = "trend_grammy_film",
                title = "Surprise Visual Album Release",
                category = TrendCategory.MUSIC,
                score = TrendScore.calculate(
                    searchGrowth = 87,
                    socialMentions = 88,
                    engagement = 86,
                    newsCoverage = 82,
                    growthVelocity = 86
                ),
                growthPercentage = 104,
                discussionsCount = "+115K discussions",
                searchVolume = "780K searches",
                timeAgo = "2h ago",
                country = Country.USA,
                chartData = listOf(22f, 38f, 52f, 65f, 78f, 82f, 85f, 86f),
                historyToday = listOf(
                    TrendHistoryPoint("00:00", 35f),
                    TrendHistoryPoint("08:00", 65f),
                    TrendHistoryPoint("16:00", 86f)
                ),
                history7Days = listOf(
                    TrendHistoryPoint("Mon", 45f),
                    TrendHistoryPoint("Wed", 60f),
                    TrendHistoryPoint("Fri", 76f),
                    TrendHistoryPoint("Sun", 86f)
                ),
                history30Days = listOf(
                    TrendHistoryPoint("W1", 30f),
                    TrendHistoryPoint("W2", 50f),
                    TrendHistoryPoint("W3", 70f),
                    TrendHistoryPoint("W4", 86f)
                ),
                sourceIcons = listOf("Spotify", "AppleMusic", "X", "TikTok"),
                isTrendingUp = true,
                isBreaking = false,
                aiAnalysis = AIAnalysis(
                    summary = "Surprise 14-track acoustic visual LP dropped at midnight, sweeping Spotify global top 10 within 3 hours.",
                    whyTrending = "Unannounced drop featuring orchestral collaborations and immersive spatial audio companion films.",
                    sentiment = SentimentBreakdown(
                        positivePercent = 84,
                        neutralPercent = 12,
                        negativePercent = 4,
                        summary = "Ecstatic fan reactions and critical acclaim for genre-blending production."
                    ),
                    expectedGrowthPercent = 86,
                    viralProbabilityPercent = 90,
                    aiConfidencePercent = 91,
                    growthTrajectory = "Rapid Exponential",
                    relatedTopics = listOf("Spotify Global #1", "Spatial Audio LP", "Grammy Contender", "Acoustic Tour", "Vinyl Edition"),
                    timeline = listOf(
                        "00:01 UTC: Album appears on all streaming platforms without advance promo",
                        "03:00 UTC: 12 songs occupy the Spotify Global Top 15"
                    )
                ),
                discussions = listOf(
                    DiscussionItem(
                        id = "d10",
                        author = "Billboard Charts",
                        handle = "@billboard",
                        platform = "X / Twitter",
                        content = "Over 85 million streams registered in the first 12 hours worldwide. An unprecedented debut.",
                        timeAgo = "2h ago",
                        likesCount = "31.2K",
                        commentsCount = "2.4K",
                        sentiment = "Positive"
                    )
                ),
                relatedNews = listOf(
                    BreakingNewsItem(
                        id = "n8",
                        headline = "Surprise Midnight Album Shatters 24-Hour Streaming Records Across 60 Countries",
                        source = "Variety Entertainment",
                        timeAgo = "2h ago",
                        imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&q=80",
                        category = TrendCategory.MUSIC,
                        trendingScore = 86
                    )
                )
            ),
            TrendItem(
                id = "trend_cinema_epic",
                title = "Sci-Fi IMAX Cinematic Odyssey",
                category = TrendCategory.ENTERTAINMENT,
                score = TrendScore.calculate(
                    searchGrowth = 85,
                    socialMentions = 84,
                    engagement = 82,
                    newsCoverage = 80,
                    growthVelocity = 83
                ),
                growthPercentage = 68,
                discussionsCount = "+54K discussions",
                searchVolume = "390K searches",
                timeAgo = "3h ago",
                country = Country.GLOBAL,
                chartData = listOf(20f, 32f, 46f, 58f, 68f, 76f, 80f, 83f),
                historyToday = listOf(
                    TrendHistoryPoint("00:00", 35f),
                    TrendHistoryPoint("08:00", 60f),
                    TrendHistoryPoint("16:00", 83f)
                ),
                history7Days = listOf(
                    TrendHistoryPoint("Mon", 42f),
                    TrendHistoryPoint("Wed", 60f),
                    TrendHistoryPoint("Fri", 74f),
                    TrendHistoryPoint("Sun", 83f)
                ),
                history30Days = listOf(
                    TrendHistoryPoint("W1", 30f),
                    TrendHistoryPoint("W2", 48f),
                    TrendHistoryPoint("W3", 68f),
                    TrendHistoryPoint("W4", 83f)
                ),
                sourceIcons = listOf("X", "IMDb", "Reddit", "YouTube"),
                isTrendingUp = true,
                isBreaking = false,
                aiAnalysis = AIAnalysis(
                    summary = "70mm IMAX space epic opens to 98% Rotten Tomatoes score and universal praise for practical visual effects and Hans Zimmer orchestral score.",
                    whyTrending = "Global premiere reviews proclaim it the definitive sci-fi masterpiece of the decade, driving sold-out 70mm film screenings worldwide.",
                    sentiment = SentimentBreakdown(
                        positivePercent = 92,
                        neutralPercent = 6,
                        negativePercent = 2,
                        summary = "Celebration of practical cinematography and profound narrative depth."
                    ),
                    expectedGrowthPercent = 82,
                    viralProbabilityPercent = 84,
                    aiConfidencePercent = 90,
                    growthTrajectory = "Steady Climb",
                    relatedTopics = listOf("70mm IMAX", "Oscars Best Picture", "Rotten Tomatoes 98%", "Practical VFX", "SciFi Masterpiece"),
                    timeline = listOf(
                        "18:00 UTC: Worldwide review embargo lifts",
                        "20:00 UTC: Presale tickets crash major theater ticketing servers"
                    )
                ),
                discussions = listOf(
                    DiscussionItem(
                        id = "d11",
                        author = "Film Critiques Weekly",
                        handle = "@cinema_review",
                        platform = "Reddit",
                        content = "A cinematic triumph. The sound design in full 12-channel IMAX literally shakes the theater seats.",
                        timeAgo = "3h ago",
                        likesCount = "12.8K",
                        commentsCount = "980",
                        sentiment = "Positive"
                    )
                ),
                relatedNews = listOf(
                    BreakingNewsItem(
                        id = "n9",
                        headline = "New Sci-Fi Masterpiece Sweeps 98% Critical Consensus and Sold-Out IMAX Bookings",
                        source = "Hollywood Reporter",
                        timeAgo = "3h ago",
                        imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&q=80",
                        category = TrendCategory.ENTERTAINMENT,
                        trendingScore = 83
                    )
                )
            )
        )
    }

    fun getBreakingNews(): List<BreakingNewsItem> {
        return listOf(
            BreakingNewsItem(
                id = "bn_1",
                headline = "Next-Gen Autonomous Multi-Agent Workflows Set New Benchmark in Software Engineering",
                source = "Wired Tech",
                timeAgo = "14m ago",
                imageUrl = "https://images.unsplash.com/photo-1677442136019-21780ecad995?w=800&q=80",
                category = TrendCategory.AI,
                trendingScore = 98,
                readTimeMinutes = 3,
                summary = "Autonomous agent clusters complete full-stack enterprise refactors in minutes, unlocking a new era of software development."
            ),
            BreakingNewsItem(
                id = "bn_2",
                headline = "Historic Fusion Plasma Test Delivers Sustained 2.8x Net Energy Milestone",
                source = "Reuters Global",
                timeAgo = "36m ago",
                imageUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=800&q=80",
                category = TrendCategory.BUSINESS,
                trendingScore = 93,
                readTimeMinutes = 4,
                summary = "Tokamak stabilization reaches 120 seconds with high-temperature superconducting coils, marking a historic leap in clean limitless power."
            ),
            BreakingNewsItem(
                id = "bn_3",
                headline = "Supply Chain Leaks Confirm Groundbreaking Silicon-Carbon Battery for Next Flagship",
                source = "Bloomberg Tech",
                timeAgo = "48m ago",
                imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80",
                category = TrendCategory.TECH,
                trendingScore = 92,
                readTimeMinutes = 3,
                summary = "High-density anode chemistry enables 4-day battery life in an ultra-slim titanium chassis."
            ),
            BreakingNewsItem(
                id = "bn_4",
                headline = "Crew Emerges Victorious from 365-Day Sealed Martian Habitat Simulation",
                source = "BBC Science",
                timeAgo = "1h ago",
                imageUrl = "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=800&q=80",
                category = TrendCategory.WORLD,
                trendingScore = 88,
                readTimeMinutes = 5,
                summary = "Astronaut crew tests closed-loop life support and regolith 3D printing inside an analog Martian dome."
            ),
            BreakingNewsItem(
                id = "bn_5",
                headline = "Kerala Tech Corridor Attracts Global Tech Giants with New AI Supercluster",
                source = "The Hindu Tech",
                timeAgo = "2h ago",
                imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&q=80",
                category = TrendCategory.TECH,
                trendingScore = 87,
                readTimeMinutes = 4,
                summary = "Kochi and Trivandrum innovation zones secure $500M in venture funding for deep-tech AI and robotics."
            )
        )
    }

    fun getTrendAnalytics(): TrendAnalytics {
        return TrendAnalytics(
            totalActiveTrends = 284,
            upwardTrendingCount = 192,
            downwardTrendingCount = 38,
            stableTrendingCount = 54,
            averageGrowthRate = "+114%",
            totalMentions24h = "4.2M",
            topTrendingCategory = "Artificial Intelligence"
        )
    }

    val trendingSearchSuggestions = listOf(
        "Artificial Intelligence",
        "iPhone 18 leaks",
        "Kerala Tech Startup Hub",
        "Clean Fusion Net Gain",
        "GTA 6 Gameplay trailer",
        "Premier League title race",
        "Artemis Mars habitat",
        "Spatial Audio visual album",
        "Quantum computing breakthrough",
        "Autonomous electric vehicles"
    )
}
