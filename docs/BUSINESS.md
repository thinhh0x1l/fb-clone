# Business Requirements

## Product Vision

A Facebook-scale social networking platform enabling 1M+ users to connect, share content, communicate in real-time, build communities, and discover content — with enterprise-grade reliability, security, and compliance.

---

## Domain Model

### Core Entities

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER DOMAIN                              │
│                                                                  │
│  User ──────── Profile ──────── Settings ──────── Privacy       │
│    │              │                  │                  │        │
│    │              ├─ Avatar          ├─ Notification    ├─ Post  │
│    │              ├─ CoverPhoto      ├─ Theme           ├─ Friend│
│    │              ├─ Bio             ├─ Language        ├─ Profile│
│    │              ├─ Work            └─ Security        └─ Search│
│    │              └─ Education                                          │
│    │                                                                   │
│    ├──── Friend ────── Block ────── Follow (optional)                  │
│    ├──── Session ───── Device ───── LoginHistory                       │
│    └──── Verification ──── Token ──── OTP                              │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         CONTENT DOMAIN                           │
│                                                                  │
│  Post ──────── Media ──────── Location ──────── Tag              │
│    │              │                │               │              │
│    ├─ Comment     ├─ Image         ├─ Place        ├─ Hashtag    │
│    │   ├─ Reply   ├─ Video         └─ Coordinates  └─ Mention    │
│    │   └─ Like    ├─ Document                                              │
│    │              └─ Link                                                  │
│    ├─ Reaction (Like, Love, Haha, Wow, Sad, Angry)                 │
│    ├─ Share ──── Original Post ──── Commentary                       │
│    ├─ Save ──── Collection ──── Folder                              │
│    └─ Report ──── Reason ──── Evidence                              │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       MESSAGING DOMAIN                           │
│                                                                  │
│  Conversation ──────── Participant ──────── Role                 │
│    │                       │                    │                 │
│    │                       ├─ Admin             ├─ Owner          │
│    │                       ├─ Member            ├─ Moderator      │
│    │                       └─ Pending           └─ Guest          │
│    │                                                               │
│    ├─ Message ──────── Reaction ──────── ReadReceipt              │
│    │   ├─ Text           ├─ Emoji           └─ DeliveredAt       │
│    │   ├─ Image          └─ Timestamp       └─ ReadAt            │
│    │   ├─ File                                                           │
│    │   └─ Link                                                           │
│    │                                                                       │
│    └─ TypingIndicator ──── OnlineStatus                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      NOTIFICATION DOMAIN                         │
│                                                                  │
│  Notification ──────── Preference ──────── Channel               │
│    │                       │                    │                 │
│    │                       ├─ In-App            ├─ Push           │
│    │                       ├─ Email             ├─ SMS            │
│    │                       └─ SMS               └─ Email          │
│    │                                                               │
│    ├─ Type (FriendRequest, Like, Comment, Message, System)       │
│    ├─ Actor (User who triggered)                                  │
│    ├─ Reference (Post, Comment, Message, etc.)                    │
│    └─ Status (Unread, Read, Dismissed)                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## Feature Specifications

### 1. Authentication & Identity

#### Registration Flow
```
Step 1: User enters email, password, name, birthday
Step 2: System validates:
  ├── Email format + uniqueness
  ├── Password strength (min 8, uppercase, lowercase, number, special)
  ├── Birthday (must be 13+ years old)
  └── Username availability (async check)
Step 3: System creates user record (email_verified = false)
Step 4: System sends verification email with 6-digit OTP
Step 5: User enters OTP (expires in 10 minutes)
Step 6: System marks email_verified = true
Step 7: System generates JWT tokens
Step 8: User redirected to onboarding
```

#### Login Flow
```
Step 1: User enters email + password
Step 2: System validates credentials
Step 3: System checks:
  ├── Email verified? → If not, resend verification
  ├── Account locked? → If yes, show lockout message
  └── Failed attempts < 5? → If no, lock for 15 minutes
Step 4: System generates:
  ├── Access token (15 minutes, RS256)
  ├── Refresh token (7 days, stored in Redis)
  └── Session record (device, IP, user agent)
Step 5: System logs login event
Step 6: User redirected to home feed
```

#### Session Management
```
Active Sessions:
├── User can view all active sessions
├── Each session shows: device, IP, location, last active
├── User can terminate individual sessions
├── User can terminate all sessions except current
└── Sessions auto-expire after 30 days of inactivity

Security:
├── New device login → Email notification
├── Suspicious location → 2FA prompt
├── Multiple concurrent sessions → Alert
└── Session invalidation on password change
```

#### Password Reset
```
Step 1: User clicks "Forgot Password"
Step 2: User enters email
Step 3: System sends reset link (expires in 1 hour)
Step 4: User clicks link, enters new password
Step 5: System:
  ├── Validates token
  ├── Updates password (BCrypt hash)
  ├── Invalidates all existing sessions
  └── Sends confirmation email
```

---

### 2. User Profile

#### Profile Data Model
```
UserProfile:
├── Basic Info
│   ├── Display Name (3-100 chars)
│   ├── Username (3-30 chars, unique, immutable after 14 days)
│   ├── Bio (max 300 chars)
│   ├── Gender (Male, Female, Other, Prefer not to say)
│   └── Birthday (required, 13+ years old)
│
├── Contact Info
│   ├── Email (verified, hidden by default)
│   ├── Phone (optional, hidden by default)
│   └── Website (optional)
│
├── Location Info
│   ├── Current City
│   ├── Hometown
│   └── Country
│
├── Work & Education
│   ├── Current Workplace
│   ├── Previous Workplaces
│   ├── Current School
│   └── Previous Schools
│
├── Media
│   ├── Avatar (max 5MB, auto-resize to 170x170, 320x320, 640x640)
│   ├── Cover Photo (max 10MB, 820x312 crop)
│   └── Profile Video (optional, max 30 seconds)
│
└── Relationship Status
    ├── Single
    ├── In a Relationship
    ├── Engaged
    ├── Married
    ├── Divorced
    └── It's Complicated
```

#### Profile Visibility Rules
```
Visibility Levels:
├── Public: Anyone can see profile, posts, friend list
├── Friends: Only friends can see profile details
├── Friends of Friends: Extended network can see basic info
├── Only Me: Profile is private
└── Custom: Select specific people/can see/excluded

What Each Visibility Controls:
├── Profile Info: Who can see bio, work, education
├── Friend List: Who can see friends
├── Posts: Default audience for new posts
├── Photos: Who can see photo albums
├── Search: Whether profile appears in search
└── Timeline: Who can post on your timeline
```

---

### 3. Friend System

#### Friend Request Lifecycle
```
States:
┌─────────┐    accept    ┌──────────┐
│ PENDING │─────────────▶│ ACCEPTED │
└─────────┘              └──────────┘
     │                        │
     │ reject                 │ unfriend
     ▼                        ▼
┌──────────┐            ┌──────────┐
│ REJECTED │            │ REMOVED  │
└──────────┘            └──────────┘
     │
     │ cancel (by requester)
     ▼
┌───────────┐
│ CANCELLED │
└───────────┘

Business Rules:
├── User cannot friend themselves
├── Duplicate pending requests blocked
├── If A sent request to B, B cannot send to A (until A's expires)
├── Blocked users cannot send requests
├── Max 1,000 pending requests per user
├── Max 5,000 friends per user
├── Requests expire after 30 days
├── Cancelled/Rejected requests: can retry after 14 days
└── Accepted friends: notification sent immediately
```

#### Friend Suggestion Algorithm
```
Weighted Scoring:
├── Mutual Friends: 50% weight
├── Same Workplace: 15% weight
├── Same Education: 10% weight
├── Same Location: 10% weight
├── Same Interests: 10% weight
├── Recently Active: 5% weight
└── Profile Views: 5% weight

Filters:
├── Exclude existing friends
├── Exclude blocked users
├── Exclude pending requests
├── Exclude users who declined in last 90 days
└── Prioritize mutual friends with highest engagement
```

---

### 4. Posts & Feed

#### Post Creation Rules
```
Content Rules:
├── Text: 0-10,000 characters (configurable)
├── Images: 0-20 images per post
├── Videos: 0-1 video per post (max 10GB, 60 minutes)
├── Links: Auto-preview with Open Graph metadata
├── Polls: 2-10 options, 1-7 day duration
├── Check-in: Optional location attachment
├── Tag People: Tag up to 50 users
└── Feeling/Activity: Predefined status types

Visibility Options:
├── Public: Anyone on/off platform
├── Friends: Only connected friends
├── Friends except: Friends minus specific people
├── Only me: Private post
├── Specific people: Custom list
└── Group: Posted to specific group (future)

Media Processing:
├── Images: Resize to 3 sizes (thumbnail, medium, large)
├── Videos: Transcode to multiple qualities (360p, 720p, 1080p)
├── Thumbnails: Auto-generate for videos
├── Face detection: Optional auto-tagging
└── Alt text: AI-generated descriptions for accessibility
```

#### News Feed Algorithm
```
Feed Ranking Factors:
├── Relationship Score (40%)
│   ├── Interaction frequency (likes, comments, messages)
│   ├── Profile views
│   ├── Tag frequency
│   └── Mutual friends engagement
│
├── Content Quality (30%)
│   ├── Post type (video > photo > text)
│   ├── Engagement velocity (likes/comments in first hour)
│   ├── Content length (medium > short > long)
│   ├── Media presence (with media > without)
│   └── Original content vs shares
│
├── Recency (20%)
│   ├── Time since posted
│   ├── Decay factor (exponential)
│   └── User's typical active hours
│
├── Diversity (10%)
│   ├── Content type variety
│   ├── Author diversity
│   └── Topic diversity
│
Negative Signals:
├── Hide post → Negative signal
├── Report post → Strong negative signal
├── Unfollow author → Very strong negative signal
└── Scroll past quickly → Mild negative signal
```

#### Feed Storage Strategy
```
Write Path (Fanout-on-Write):
├── User creates post
├── Save to PostgreSQL (source of truth)
├── Publish PostCreated event to Kafka
├── Feed Service receives event
├── Get user's followers (from social graph cache)
├── For each follower (< 10K followers):
│   ├── Add postId to follower's Redis feed (Sorted Set)
│   └── Feed = [postId:timestamp, postId:timestamp, ...]
└── For celebrity followers (> 10K):
    └── Skip fanout (handled on read)

Read Path:
├── User requests feed
├── Check Redis feed cache
├── If cache hit:
│   ├── Get post IDs from Redis (paginated)
│   ├── Batch fetch post details from PostgreSQL
│   ├── Enrich with user data, reaction counts
│   └── Return to client
├── If cache miss:
│   ├── Query PostgreSQL for user's friends' posts
│   ├── Populate Redis cache
│   └── Return to client
└── Cursor-based pagination (timestamp-based)
```

---

### 5. Comments

#### Threaded Comments
```
Structure:
Post
├── Comment 1 (root)
│   ├── Reply 1.1 (depth: 1)
│   │   ├── Reply 1.1.1 (depth: 2)
│   │   └── Reply 1.1.2 (depth: 2)
│   └── Reply 1.2 (depth: 1)
└── Comment 2 (root)

Rules:
├── Max nesting depth: 3 levels
├── Replies show @mention of parent author
├── Deleted comments: children become orphaned (root-level)
├── Comment sorting: Newest, Oldest, Most Liked
└── Lazy loading: Load 10 comments initially, load more on scroll
```

---

### 6. Messaging

#### Real-time Architecture
```
WebSocket Connection:
├── Client connects with JWT
├── Server validates token
├── Server registers connection in Redis:
│   Key: user:socket:{userId}
│   Value: {socketId, serverId, connectedAt}
├── Server subscribes to user's notification channel
└── Heartbeat every 30 seconds

Message Flow:
├── User A sends message
├── Client emits: message:send
├── Server receives:
│   ├── Validate conversation membership
│   ├── Rate limit check
│   ├── Save message to PostgreSQL
│   ├── Update conversation last_message
│   └── Publish MessageSent event
├── Server pushes to User B via WebSocket
├── If User B offline:
│   ├── Store in notification queue
│   ├── Send push notification (if enabled)
│   └── Send email digest (if enabled)
└── User A receives: message:sent (ack)
```

#### Message Features
```
Text Messages:
├── Max 10,000 characters
├── Support @mentions
├── Support emoji
├── Support URLs (auto-preview)
└── Markdown support (basic)

Media Messages:
├── Images: Auto-resize, thumbnail
├── Videos: Transcode, thumbnail
├── Files: Max 100MB
├── Audio: Voice messages (future)
└── GIFs: Integration with Giphy API (future)

Advanced Features:
├── Message editing (within 15 minutes)
├── Message deletion (within 24 hours)
├── Message reactions (emoji)
├── Message pinning (group chats)
├── Message threading (reply to specific message)
├── Read receipts (sent, delivered, read)
├── Typing indicators (with debounce)
├── Online/offline status
├── Message search (within conversation)
└── Voice/Video calls (future)
```

---

### 7. Notifications

#### Notification Types
```
Social Notifications:
├── FRIEND_REQUEST: "X sent you a friend request"
├── FRIEND_ACCEPT: "X accepted your friend request"
├── POST_LIKE: "X liked your post"
├── POST_COMMENT: "X commented on your post"
├── COMMENT_LIKE: "X liked your comment"
├── COMMENT_REPLY: "X replied to your comment"
├── POST_MENTION: "X mentioned you in a post"
├── COMMENT_MENTION: "X mentioned you in a comment"
├── POST_SHARE: "X shared your post"
└── BIRTHDAY: "X's birthday is today"

Messaging Notifications:
├── NEW_MESSAGE: "X sent you a message"
├── GROUP_MESSAGE: "X sent a message in Y"
└── MISSED_CALL: "You missed a call from X"

System Notifications:
├── SECURITY: "New login from Chrome on Windows"
├── VERIFICATION: "Your email has been verified"
├── POLICY: "Your post was removed for violating community standards"
├── REMINDER: "You have 3 unread messages"
└── PROMOTION: "See what's trending" (future)
```

#### Delivery Channels
```
In-App (Real-time):
├── WebSocket push
├── Notification badge update
├── Sound notification (optional)
└── Desktop notification (optional)

Email:
├── Immediate: Security alerts, password reset
├── Digest: Daily/weekly summary (configurable)
├── Marketing: Product updates (opt-in)
└── Frequency cap: Max 3 emails per day

Push (Mobile - Future):
├── High priority: Messages, friend requests
├── Normal: Likes, comments
├── Low: System notifications
└── Quiet hours: No push between 10PM-8AM

SMS (Future):
├── Only for: 2FA, security alerts
├── Frequency: Max 5 SMS per day
└── Opt-out: Not possible for security SMS
```

---

### 8. Search

#### Search Architecture
```
Elasticsearch Indices:
├── users_index
│   ├── Fields: id, username, displayName, bio, avatar, location
│   ├── Analyzers: standard, edge_ngram (autocomplete)
│   ├── Boost: username (exact > prefix > fuzzy)
│   └── Filters: blocked users, deleted accounts
│
├── posts_index
│   ├── Fields: id, content, userId, visibility, hashtags, mentions
│   ├── Analyzers: standard, custom (social)
│   ├── Boost: recency, engagement, author relevance
│   └── Filters: visibility, blocked users
│
├── hashtags_index
│   ├── Fields: tag, count, trendingScore
│   ├── Real-time aggregation from posts
│   └── Trending: Top 100 hashtags (last 24 hours)
│
└── places_index
    ├── Fields: id, name, city, country, coordinates
    ├── Geospatial search
    └── Boost: popularity, distance

Search Features:
├── Autocomplete: As-you-type suggestions (< 100ms)
├── Fuzzy search: Handle typos (Levenshtein distance ≤ 2)
├── Faceted search: Filter by type, date, location
├── Personalized results: Based on social graph
├── Recent searches: Stored locally (30 days)
├── Trending searches: Aggregated from all users
└── Safe search: Content moderation integration
```

---

### 9. Media

#### Upload Pipeline
```
Client Upload:
├── Drag & drop or click to select
├── Client-side validation (size, type)
├── Progress indicator
├── Client-side compression (optional)
└── Chunked upload for large files

Server Processing:
├── Receive upload
├── Validate file type + size
├── Generate unique filename
├── Upload to MinIO/S3
├── Create media record in DB
├── Queue processing job:
│   ├── Image: Resize to 3 sizes
│   ├── Video: Transcode to 3 qualities
│   ├── Generate thumbnails
│   ├── Extract metadata (EXIF)
│   ├── Content moderation scan (future)
│   └── Alt text generation (future)
└── Return media URL to client

Storage Structure:
uploads/
├── {userId}/
│   ├── avatars/
│   │   ├── {uuid}_170x170.jpg
│   │   ├── {uuid}_320x320.jpg
│   │   └── {uuid}_640x640.jpg
│   ├── covers/
│   │   └── {uuid}_820x312.jpg
│   └── posts/
│       ├── {uuid}_original.jpg
│       ├── {uuid}_large.jpg
│       ├── {uuid}_medium.jpg
│       └── {uuid}_thumbnail.jpg
```

---

### 10. Content Moderation

#### Moderation Pipeline
```
Automated Filtering:
├── Image Analysis
│   ├── Nudity detection (NSFW)
│   ├── Violence detection
│   ├── Hate symbol detection
│   └── Spam detection
│
├── Text Analysis
│   ├── Hate speech detection
│   ├── Bullying detection
│   ├── Spam keyword detection
│   ├── Phishing URL detection
│   └── Sentiment analysis
│
└── Video Analysis
    ├── Frame sampling (1 per second)
    ├── Image analysis on frames
    ├── Audio transcription
    └── Text analysis on transcript

Action Levels:
├── Warning: Show content warning, reduce distribution
├── Restrict: Remove from recommendations, limit visibility
├── Remove: Delete content, notify user
├── Suspend: Temporary account suspension (1-30 days)
└── Ban: Permanent account removal

Appeals Process:
├── User can appeal within 30 days
├── Appeal reviewed by human moderator
├── Decision within 48 hours
├── User notified of decision
└── One appeal per action
```

---

## Data Retention & Privacy

### GDPR Compliance

```
Data Subject Rights:
├── Right to Access: Export all personal data
├── Right to Rectification: Edit any personal data
├── Right to Erasure: Delete account + all data
├── Right to Portability: Export in machine-readable format
├── Right to Object: Opt-out of data processing
└── Right to Restrict: Limit data processing

Data Retention:
├── Active accounts: Indefinite
├── Deactivated accounts: 3 years, then delete
├── Deleted accounts: 30-day grace period, then permanent delete
├── Posts: 5 years or until user deletes
├── Messages: 3 years or until user deletes
├── Logs: 1 year
├── Analytics: 2 years (anonymized)
└── Backup data: 90 days

Data Export Format:
├── Personal Info: JSON
├── Posts: JSON + Media ZIP
├── Messages: JSON
├── Photos: Original quality ZIP
├── Friend List: JSON
└── Activity Log: JSON
```

### Data Classification

```
Sensitive Data (Encrypted at Rest):
├── Passwords (BCrypt hash)
├── Email addresses
├── Phone numbers
├── IP addresses
├── Payment information (future)
└── Government IDs (for verification)

Internal Data:
├── User IDs
├── Session tokens
├── Device fingerprints
├── Usage analytics
└── System logs

Public Data:
├── Display names
├── Avatars
├── Public posts
├── Public profiles
└── Follower/following counts
```

---

## Success Metrics

### Key Performance Indicators (KPIs)

| Category | Metric | Target (MVP) | Target (Growth) | Target (Scale) |
|----------|--------|-------------|-----------------|----------------|
| **Engagement** | DAU/MAU | 20% | 35% | 50% |
| | Avg. Session Duration | 10 min | 20 min | 30 min |
| | Posts per User/Day | 0.5 | 2 | 5 |
| | Comments per Post | 2 | 5 | 10 |
| | Messages per User/Day | 5 | 15 | 30 |
| **Growth** | Monthly New Users | 1K | 50K | 500K |
| | User Retention (D7) | 30% | 45% | 60% |
| | User Retention (D30) | 15% | 25% | 40% |
| | Viral Coefficient | 0.5 | 0.8 | 1.2 |
| **Performance** | Feed Load Time | < 500ms | < 200ms | < 100ms |
| | Message Delivery | < 100ms | < 50ms | < 20ms |
| | Image Upload (5MB) | < 3s | < 1.5s | < 1s |
| | API Response (p95) | < 200ms | < 100ms | < 50ms |
| | API Response (p99) | < 500ms | < 200ms | < 100ms |
| **Reliability** | Uptime | 99.5% | 99.9% | 99.99% |
| | Error Rate | < 1% | < 0.1% | < 0.01% |
| | Data Durability | 99.9% | 99.99% | 99.999% |

---

## Monetization Strategy (Future)

### Revenue Streams
```
Advertising:
├── News Feed Ads: Native ads in feed
├── Stories Ads: Full-screen ads between stories
├── Messenger Ads: Sponsored messages
├── Marketplace Ads: Product listings
└── Audience Network: Ads on third-party apps

Premium Features (Future):
├── Facebook Pro: Ad-free, advanced analytics
├── Facebook Business: Page management tools
├── Facebook Dating: Premium matching features
└── Facebook Gaming: Streaming, subscriptions

Marketplace:
├── Transaction fees (5%)
├── Payment processing (2.9% + $0.30)
└── Promoted listings

Business Tools:
├── Page management: $10-50/month
├── Analytics dashboard: $20-100/month
├── Customer support tools: $50-200/month
└── API access: $100-500/month
```

---

## Roadmap

### Phase 1: MVP (Months 1-3)
- [ ] Authentication (email/password)
- [ ] User profiles
- [ ] Friend system
- [ ] Posts (text, images)
- [ ] News feed
- [ ] Comments
- [ ] Reactions
- [ ] Basic messaging
- [ ] Notifications
- [ ] Search

### Phase 2: Growth (Months 4-6)
- [ ] Stories
- [ ] Groups
- [ ] Events
- [ ] Marketplace (basic)
- [ ] Video upload
- [ ] Advanced messaging (group chat, reactions)
- [ ] Content moderation (basic)
- [ ] Analytics dashboard

### Phase 3: Scale (Months 7-12)
- [ ] Live streaming
- [ ] Reels / Short videos
- [ ] AI-powered feed ranking
- [ ] Advanced moderation (ML)
- [ ] Monetization (ads)
- [ ] Mobile apps (React Native)
- [ ] API for third-party apps
- [ ] Internationalization (i18n)

### Phase 4: Enterprise (Year 2+)
- [ ] Enterprise features (pages, business tools)
- [ ] Advanced analytics
- [ ] Customer support tools
- [ ] Payment integration
- [ ] Marketplace (advanced)
- [ ] Dating features
- [ ] Gaming platform
- [ ] VR/AR features
