export type UserRole = 'USER' | 'MOSQUE_ADMIN' | 'MODERATOR' | 'SUPER_ADMIN';

export interface User {
  id: string;
  firebaseUid: string;
  email: string;
  displayName: string;
  phoneNumber?: string;
  photoUrl?: string;
  role: UserRole;
  points: number;
  isActive: boolean;
  isVerified: boolean;
  twoFactorEnabled?: boolean;
  createdAt?: string;
}

export interface Facility {
  id: string;
  code: string;
  name: string;
  description?: string;
  iconName: string;
  isActive: boolean;
}

export interface MosqueImage {
  id: string;
  imageUrl: string;
  caption?: string;
  isCover: boolean;
  displayOrder: number;
}

export interface Mosque {
  id: string;
  name: string;
  slug: string;
  description: string;
  address: string;
  city: string;
  state?: string;
  country: string;
  postalCode?: string;
  latitude: number;
  longitude: number;
  distanceKm?: number;
  contactPhone?: string;
  contactEmail?: string;
  websiteUrl?: string;
  liveStreamUrl?: string;
  isVerified: boolean;
  status: 'ACTIVE' | 'PENDING' | 'SUSPENDED';
  coverImageUrl?: string;
  facilities: string[];
  nextPrayerName?: string;
  nextPrayerTime?: string;
}

export interface MosqueDetail extends Mosque {
  images: MosqueImage[];
  facilityObjects?: Facility[];
  adminUser?: {
    id: string;
    displayName: string;
    email: string;
  };
}

export interface NextPrayerInfo {
  name: string;
  adhanTime: string;
  iqamahTime: string;
  remainingMinutes: number;
}

export interface FridayJummahSchedule {
  batch1Time: string;
  batch2Time?: string;
  language?: string;
  khatibName?: string;
}

export interface PrayerTimesDto {
  date: string;
  fajrAdhan: string;
  fajrIqamah: string;
  sunrise: string;
  dhuhrAdhan: string;
  dhuhrIqamah: string;
  asrAdhan: string;
  asrIqamah: string;
  maghribAdhan: string;
  maghribIqamah: string;
  ishaAdhan: string;
  ishaIqamah: string;
  nextPrayer: NextPrayerInfo;
  jummahSchedule?: FridayJummahSchedule;
}

export interface IqamahScheduleDto {
  id?: string;
  mosqueId: string;
  fajrType: 'OFFSET_AFTER_ADHAN' | 'FIXED_TIME';
  fajrOffsetMinutes?: number;
  fajrFixedTime?: string;
  dhuhrType: 'OFFSET_AFTER_ADHAN' | 'FIXED_TIME';
  dhuhrOffsetMinutes?: number;
  dhuhrFixedTime?: string;
  asrType: 'OFFSET_AFTER_ADHAN' | 'FIXED_TIME';
  asrOffsetMinutes?: number;
  asrFixedTime?: string;
  maghribType: 'OFFSET_AFTER_ADHAN' | 'FIXED_TIME';
  maghribOffsetMinutes?: number;
  maghribFixedTime?: string;
  ishaType: 'OFFSET_AFTER_ADHAN' | 'FIXED_TIME';
  ishaOffsetMinutes?: number;
  ishaFixedTime?: string;
  jummah1Time?: string;
  jummah2Time?: string;
  jummahKhutbahLanguage?: string;
}

export interface MosqueReview {
  id: string;
  mosqueId: string;
  userId: string;
  userName: string;
  userPhotoUrl?: string;
  ratingOverall: number;
  ratingCleanliness: number;
  ratingFacilities: number;
  ratingWomensArea: number;
  ratingParking: number;
  reviewText: string;
  status: 'PUBLISHED' | 'FLAGGED' | 'REMOVED';
  createdAt: string;
  updatedAt?: string;
}

export interface RatingSummary {
  averageOverall: number;
  averageCleanliness: number;
  averageFacilities: number;
  averageWomensArea: number;
  averageParking: number;
  totalReviews: number;
}

export interface MosqueAnswer {
  id: string;
  questionId: string;
  userId: string;
  userName: string;
  answerText: string;
  isOfficialMosqueAdmin: boolean;
  status: 'PUBLISHED' | 'FLAGGED' | 'REMOVED';
  createdAt: string;
}

export interface MosqueQuestion {
  id: string;
  mosqueId: string;
  userId: string;
  userName: string;
  questionText: string;
  status: 'PUBLISHED' | 'FLAGGED' | 'REMOVED';
  createdAt: string;
  answers: MosqueAnswer[];
}

export interface MosqueEvent {
  id: string;
  mosqueId: string;
  title: string;
  description: string;
  eventType: 'HALAQAH' | 'WORKSHOP' | 'YOUTH_PROGRAM' | 'COMMUNITY_DINNER' | 'LECTURE' | 'OTHER';
  audience: 'ALL' | 'BROTHERS' | 'SISTERS' | 'YOUTH' | 'KIDS';
  startDateTime: string;
  endDateTime: string;
  locationDetails?: string;
  speakerName?: string;
  bannerImageUrl?: string;
  registrationUrl?: string;
  isCancelled: boolean;
}

export interface MosqueKhutbah {
  id: string;
  mosqueId: string;
  khutbahDate: string;
  topic: string;
  khatibName: string;
  batchNumber: number;
  khutbahTime: string;
  adhaanTime?: string;
  iqamahTime?: string;
  language: string;
  streamUrl?: string;
  recordingUrl?: string;
  notes?: string;
}

export interface MosqueSubmission {
  id: string;
  submitterId: string;
  submitterEmail?: string;
  name: string;
  description: string;
  address: string;
  city: string;
  country: string;
  latitude: number;
  longitude: number;
  contactPhone?: string;
  websiteUrl?: string;
  liveStreamUrl?: string;
  facilityCodes: string[];
  imageUrls: string[];
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  reviewComments?: string;
  createdAt: string;
}

export interface MosqueClaim {
  id: string;
  mosqueId: string;
  mosqueName?: string;
  claimantId: string;
  claimantEmail?: string;
  fullName: string;
  phoneNumber: string;
  officialEmail: string;
  positionInMosque: string;
  proofDocumentUrl: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  reviewComments?: string;
  createdAt: string;
}

export interface ContentFlag {
  id: string;
  targetType: 'REVIEW' | 'QUESTION' | 'ANSWER';
  targetId: string;
  reporterId: string;
  reason: string;
  status: 'PENDING' | 'RESOLVED' | 'DISMISSED';
  reviewerNotes?: string;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  error?: string;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
}

export interface OsmIngestResult {
  totalElementsFetched: number;
  mosquesInserted: number;
  duplicatesSkipped: number;
  facilitiesAttached: number;
  insertedMosqueNames: string[];
  durationMs: number;
}
