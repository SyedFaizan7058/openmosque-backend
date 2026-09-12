import type {
  Facility,
  MosqueDetail,
  PrayerTimesDto,
  MosqueReview,
  RatingSummary,
  MosqueQuestion,
  MosqueEvent,
  MosqueKhutbah,
  MosqueSubmission,
  MosqueClaim,
  ContentFlag,
  User,
} from '../types';

export const MOCK_FACILITIES: Facility[] = [
  { id: 'fac-1', code: 'WUDU_AREA', name: 'Wudu Facilities', description: 'Clean, dedicated ablution areas with hot water', iconName: 'Droplets', isActive: true },
  { id: 'fac-2', code: 'WOMENS_SECTION', name: "Women's Prayer Hall", description: "Spacious, dedicated women's section with separate entrance and audio", iconName: 'Users', isActive: true },
  { id: 'fac-3', code: 'PARKING', name: 'Car Parking', description: 'On-site parking spaces for congregation members', iconName: 'Car', isActive: true },
  { id: 'fac-4', code: 'WHEELCHAIR', name: 'Wheelchair Accessible', description: 'Ramp and elevator access with accessible washrooms', iconName: 'Accessibility', isActive: true },
  { id: 'fac-5', code: 'LIBRARY', name: 'Islamic Library', description: 'Extensive collection of Quran, Hadith, and Islamic books', iconName: 'BookOpen', isActive: true },
  { id: 'fac-6', code: 'AIR_CONDITIONING', name: 'Climate Controlled', description: 'Central AC and heating in main prayer halls', iconName: 'Wind', isActive: true },
  { id: 'fac-7', code: 'FUNERAL_SERVICE', name: 'Janazah Services', description: 'Mortuary and funeral prayer support', iconName: 'HeartHandshake', isActive: true },
  { id: 'fac-8', code: 'HALAL_FOOD_NEARBY', name: 'Halal Food Nearby', description: 'Multiple halal eateries within walking distance', iconName: 'Utensils', isActive: true },
];

export const MOCK_MOSQUES: MosqueDetail[] = [
  {
    id: '18558e48-a2aa-4219-9583-54cd9a84a76c',
    name: 'East London Mosque & London Muslim Centre',
    slug: 'east-london-mosque-london',
    description: 'One of the largest mosques in Europe, serving the community for over a century with daily congregational prayers, educational classes, and civic outreach.',
    address: '82-92 Whitechapel Rd',
    city: 'London',
    state: 'Greater London',
    country: 'United Kingdom',
    postalCode: 'E1 1JQ',
    latitude: 51.5173,
    longitude: -0.0658,
    distanceKm: 0.8,
    contactPhone: '+44 20 7654 3210',
    contactEmail: 'info@eastlondonmosque.org.uk',
    websiteUrl: 'https://www.eastlondonmosque.org.uk',
    liveStreamUrl: 'https://www.youtube.com/watch?v=live-demo-stream',
    isVerified: true,
    status: 'ACTIVE',
    coverImageUrl: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80',
    facilities: ['WUDU_AREA', 'WOMENS_SECTION', 'PARKING', 'WHEELCHAIR', 'LIBRARY', 'AIR_CONDITIONING', 'FUNERAL_SERVICE', 'HALAL_FOOD_NEARBY'],
    nextPrayerName: 'Asr',
    nextPrayerTime: '17:15',
    images: [
      {
        id: 'img-1',
        imageUrl: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&auto=format&fit=crop&q=80',
        caption: 'Main Entrance & Minaret',
        isCover: true,
        displayOrder: 1,
      },
      {
        id: 'img-2',
        imageUrl: 'https://images.unsplash.com/photo-1564769625905-50e93615e769?w=1200&auto=format&fit=crop&q=80',
        caption: 'Main Prayer Hall during Friday Jumuah',
        isCover: false,
        displayOrder: 2,
      },
      {
        id: 'img-3',
        imageUrl: 'https://images.unsplash.com/photo-1519817650390-64a93db51149?w=1200&auto=format&fit=crop&q=80',
        caption: 'Intricate Mihrab and Minbar',
        isCover: false,
        displayOrder: 3,
      },
    ],
    adminUser: {
      id: 'usr-imam-202',
      displayName: 'Imam Tariq Al-Banna',
      email: 'imam.tariq@eastlondonmosque.org',
    },
  },
  {
    id: 'b4a8e2a3-25bc-488f-9a3b-240192e105e4',
    name: 'Cambridge Central Mosque',
    slug: 'cambridge-central-mosque-cambridge',
    description: "Europe's first eco-friendly mosque, featuring sustainable timber architecture, natural lighting, and peaceful Islamic gardens.",
    address: '309-313 Mill Rd',
    city: 'Cambridge',
    state: 'Cambridgeshire',
    country: 'United Kingdom',
    postalCode: 'CB1 3DF',
    latitude: 52.1983,
    longitude: 0.1444,
    distanceKm: 2.3,
    contactPhone: '+44 1223 654321',
    contactEmail: 'contact@cambridgecentralmosque.org',
    websiteUrl: 'https://cambridgecentralmosque.org',
    liveStreamUrl: 'https://cambridgecentralmosque.org/live',
    isVerified: true,
    status: 'ACTIVE',
    coverImageUrl: 'https://images.unsplash.com/photo-1584551246679-0daf3d275d0f?w=800&auto=format&fit=crop&q=80',
    facilities: ['WUDU_AREA', 'WOMENS_SECTION', 'WHEELCHAIR', 'LIBRARY', 'AIR_CONDITIONING'],
    nextPrayerName: 'Asr',
    nextPrayerTime: '17:15',
    images: [
      {
        id: 'img-ccm-1',
        imageUrl: 'https://images.unsplash.com/photo-1584551246679-0daf3d275d0f?w=1200&auto=format&fit=crop&q=80',
        caption: 'Iconic timber arches and natural dome skylights',
        isCover: true,
        displayOrder: 1,
      },
    ],
  },
  {
    id: 'c9f110c4-9182-4fa8-b2a1-1294871029ba',
    name: 'Birmingham Central Mosque',
    slug: 'birmingham-central-mosque-birmingham',
    description: 'A landmark community institution in the West Midlands with capacity for over 6,000 worshippers, community food banks, and educational center.',
    address: '180 Belgrave Middleway',
    city: 'Birmingham',
    state: 'West Midlands',
    country: 'United Kingdom',
    postalCode: 'B12 0XS',
    latitude: 52.4678,
    longitude: -1.8906,
    distanceKm: 4.1,
    contactPhone: '+44 121 440 5355',
    contactEmail: 'enquiries@birminghamcentralmosque.org.uk',
    websiteUrl: 'https://birminghamcentralmosque.org.uk',
    isVerified: true,
    status: 'ACTIVE',
    coverImageUrl: 'https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=800&auto=format&fit=crop&q=80',
    facilities: ['WUDU_AREA', 'WOMENS_SECTION', 'PARKING', 'WHEELCHAIR', 'FUNERAL_SERVICE'],
    nextPrayerName: 'Asr',
    nextPrayerTime: '17:15',
    images: [
      {
        id: 'img-bcm-1',
        imageUrl: 'https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=1200&auto=format&fit=crop&q=80',
        caption: 'Golden dome and courtyard',
        isCover: true,
        displayOrder: 1,
      },
    ],
  },
  {
    id: 'd8c47b59-4829-43c2-a9b0-983174981723',
    name: 'Islamic Cultural Centre of New York',
    slug: 'islamic-cultural-centre-new-york',
    description: 'Prominent mosque and cultural institution on Manhattan Upper East Side, oriented precisely towards Mecca.',
    address: '1711 3rd Ave',
    city: 'New York',
    state: 'NY',
    country: 'United States',
    postalCode: '10029',
    latitude: 40.7850,
    longitude: -73.9535,
    distanceKm: 12.5,
    contactPhone: '+1 212 722 5234',
    contactEmail: 'contact@iccny.org',
    websiteUrl: 'https://iccny.org',
    isVerified: true,
    status: 'ACTIVE',
    coverImageUrl: 'https://images.unsplash.com/photo-1564769625905-50e93615e769?w=800&auto=format&fit=crop&q=80',
    facilities: ['WUDU_AREA', 'WOMENS_SECTION', 'WHEELCHAIR', 'AIR_CONDITIONING', 'LIBRARY'],
    nextPrayerName: 'Asr',
    nextPrayerTime: '17:15',
    images: [
      {
        id: 'img-icc-1',
        imageUrl: 'https://images.unsplash.com/photo-1564769625905-50e93615e769?w=1200&auto=format&fit=crop&q=80',
        caption: 'New York landmark mosque architecture',
        isCover: true,
        displayOrder: 1,
      },
    ],
  },
  {
    id: 'e1d2c3b4-5678-90ab-cdef-1234567890ab',
    name: 'Edinburgh Central Mosque',
    slug: 'edinburgh-central-mosque-edinburgh',
    description: 'Situated near the University of Edinburgh, featuring King Fahd Hall and serving students and residents across Scotland.',
    address: '20 Potterrow',
    city: 'Edinburgh',
    state: 'Scotland',
    country: 'United Kingdom',
    postalCode: 'EH8 9BL',
    latitude: 55.9443,
    longitude: -3.1873,
    distanceKm: 6.8,
    contactPhone: '+44 131 667 1777',
    contactEmail: 'info@edinburghcentralmosque.org.uk',
    websiteUrl: 'https://edinburghcentralmosque.org.uk',
    isVerified: false,
    status: 'ACTIVE',
    coverImageUrl: 'https://images.unsplash.com/photo-1584551246679-0daf3d275d0f?w=800&auto=format&fit=crop&q=80',
    facilities: ['WUDU_AREA', 'WOMENS_SECTION', 'HALAL_FOOD_NEARBY'],
    nextPrayerName: 'Asr',
    nextPrayerTime: '17:15',
    images: [
      {
        id: 'img-ecm-1',
        imageUrl: 'https://images.unsplash.com/photo-1584551246679-0daf3d275d0f?w=1200&auto=format&fit=crop&q=80',
        caption: 'Exterior elevation and minaret',
        isCover: true,
        displayOrder: 1,
      },
    ],
  },
];

export const getMockPrayerTimes = (_mosqueId: string, _dateStr?: string): PrayerTimesDto => {
  const now = new Date();
  const currentHour = now.getHours();

  let nextPrayerName = 'Fajr';
  let adhan = '05:12';
  let iqamah = '05:35';
  let remainingMinutes = 45;

  if (currentHour < 5) {
    nextPrayerName = 'Fajr';
    adhan = '05:12';
    iqamah = '05:35';
    remainingMinutes = (5 - currentHour) * 60;
  } else if (currentHour < 12) {
    nextPrayerName = 'Dhuhr';
    adhan = '13:05';
    iqamah = '13:30';
    remainingMinutes = (13 - currentHour) * 60;
  } else if (currentHour < 16) {
    nextPrayerName = 'Asr';
    adhan = '16:45';
    iqamah = '17:15';
    remainingMinutes = (17 - currentHour) * 60;
  } else if (currentHour < 19) {
    nextPrayerName = 'Maghrib';
    adhan = '19:42';
    iqamah = '19:50';
    remainingMinutes = (20 - currentHour) * 60;
  } else if (currentHour < 22) {
    nextPrayerName = 'Isha';
    adhan = '21:15';
    iqamah = '21:40';
    remainingMinutes = (22 - currentHour) * 60;
  }

  return {
    date: new Date().toISOString().split('T')[0],
    fajrAdhan: '05:12',
    fajrIqamah: '05:35',
    sunrise: '06:28',
    dhuhrAdhan: '13:05',
    dhuhrIqamah: '13:30',
    asrAdhan: '16:45',
    asrIqamah: '17:15',
    maghribAdhan: '19:42',
    maghribIqamah: '19:50',
    ishaAdhan: '21:15',
    ishaIqamah: '21:40',
    nextPrayer: {
      name: nextPrayerName,
      adhanTime: adhan,
      iqamahTime: iqamah,
      remainingMinutes: Math.max(12, remainingMinutes),
    },
    jummahSchedule: {
      batch1Time: '13:15',
      batch2Time: '14:00',
      language: 'English & Arabic',
      khatibName: 'Shaykh Abdul Qayum',
    },
  };
};

export const MOCK_REVIEWS: MosqueReview[] = [
  {
    id: 'rev-1',
    mosqueId: '18558e48-a2aa-4219-9583-54cd9a84a76c',
    userId: 'usr-contributor-101',
    userName: 'Ahmad Al-Mansoor',
    userPhotoUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80',
    ratingOverall: 5,
    ratingCleanliness: 5,
    ratingFacilities: 5,
    ratingWomensArea: 5,
    ratingParking: 4,
    reviewText: 'Pristine wudu area and very spiritually uplifted atmosphere. The Friday khutbah translation in English is extremely clear and relevant.',
    status: 'PUBLISHED',
    createdAt: '2026-03-02T14:20:00Z',
  },
  {
    id: 'rev-2',
    mosqueId: '18558e48-a2aa-4219-9583-54cd9a84a76c',
    userId: 'usr-sister-99',
    userName: 'Maryam Siddiqui',
    userPhotoUrl: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80',
    ratingOverall: 5,
    ratingCleanliness: 5,
    ratingFacilities: 4,
    ratingWomensArea: 5,
    ratingParking: 3,
    reviewText: "The sisters' prayer hall is spacious, well-maintained, and has dedicated mother-and-baby facilities. Parking on Whitechapel can be tight, so use public transit.",
    status: 'PUBLISHED',
    createdAt: '2026-02-18T10:15:00Z',
  },
];

export const MOCK_RATING_SUMMARY: RatingSummary = {
  averageOverall: 4.9,
  averageCleanliness: 4.8,
  averageFacilities: 4.7,
  averageWomensArea: 4.9,
  averageParking: 3.8,
  totalReviews: 48,
};

export const MOCK_QUESTIONS: MosqueQuestion[] = [
  {
    id: 'q-1',
    mosqueId: '18558e48-a2aa-4219-9583-54cd9a84a76c',
    userId: 'usr-sister-99',
    userName: 'Maryam Siddiqui',
    questionText: 'Is elevator access available directly to the upper women prayer gallery for elderly worshippers?',
    status: 'PUBLISHED',
    createdAt: '2026-02-25T11:00:00Z',
    answers: [
      {
        id: 'ans-1',
        questionId: 'q-1',
        userId: 'usr-imam-202',
        userName: 'Imam Tariq Al-Banna',
        answerText: 'Assalamu alaikum sister. Yes, two elevators are situated right beside the Main Entrance B with direct access to both floors.',
        isOfficialMosqueAdmin: true,
        status: 'PUBLISHED',
        createdAt: '2026-02-25T13:30:00Z',
      },
    ],
  },
  {
    id: 'q-2',
    mosqueId: '18558e48-a2aa-4219-9583-54cd9a84a76c',
    userId: 'usr-visitor-12',
    userName: 'Bilal Khan',
    questionText: 'Are Taraweeh prayers 8 or 20 Rakaats during the holy month of Ramadan?',
    status: 'PUBLISHED',
    createdAt: '2026-03-01T09:40:00Z',
    answers: [
      {
        id: 'ans-2',
        questionId: 'q-2',
        userId: 'usr-imam-202',
        userName: 'Imam Tariq Al-Banna',
        answerText: 'Taraweeh is 20 Rakaats following the Hanafi tradition, with full Quran completion by the 27th night inshaAllah.',
        isOfficialMosqueAdmin: true,
        status: 'PUBLISHED',
        createdAt: '2026-03-01T11:20:00Z',
      },
    ],
  },
];

export const MOCK_EVENTS: MosqueEvent[] = [
  {
    id: 'evt-1',
    mosqueId: '18558e48-a2aa-4219-9583-54cd9a84a76c',
    title: 'Surah Al-Kahf Tafseer & Reflection Circle',
    description: 'Weekly in-depth contemplation on the 4 trials and spiritual lessons of Surah Al-Kahf with Shaykh Abdul Qayum.',
    eventType: 'HALAQAH',
    audience: 'ALL',
    startDateTime: '2026-09-18T18:00:00Z',
    endDateTime: '2026-09-18T19:30:00Z',
    locationDetails: 'Ground Floor Main Seminar Hall',
    speakerName: 'Shaykh Abdul Qayum',
    bannerImageUrl: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80',
    registrationUrl: 'https://eastlondonmosque.org.uk/events/kahf-tafseer',
    isCancelled: false,
  },
  {
    id: 'evt-2',
    mosqueId: '18558e48-a2aa-4219-9583-54cd9a84a76c',
    title: 'Youth Archery & Sunnah Sports Workshop',
    description: 'Physical fitness and Sunnah sports for teenagers aged 12-18. Certified instructors and equipment provided.',
    eventType: 'YOUTH_PROGRAM',
    audience: 'YOUTH',
    startDateTime: '2026-09-20T14:00:00Z',
    endDateTime: '2026-09-20T17:00:00Z',
    locationDetails: 'Community Sports Hall',
    speakerName: 'Coach Zayd & Youth Leaders',
    isCancelled: false,
  },
];

export const MOCK_KHUTBAHS: MosqueKhutbah[] = [
  {
    id: 'khut-1',
    mosqueId: '18558e48-a2aa-4219-9583-54cd9a84a76c',
    khutbahDate: '2026-09-11',
    topic: 'Purification of Heart in the Digital Age',
    khatibName: 'Shaykh Abdul Qayum',
    batchNumber: 1,
    khutbahTime: '13:00:00',
    adhaanTime: '12:45:00',
    iqamahTime: '13:30:00',
    language: 'English & Arabic',
    streamUrl: 'https://www.youtube.com/watch?v=khutbah-1',
    recordingUrl: 'https://www.youtube.com/watch?v=khutbah-1',
    notes: 'Recorded live on Friday 11 September 2026.',
  },
  {
    id: 'khut-2',
    mosqueId: '18558e48-a2aa-4219-9583-54cd9a84a76c',
    khutbahDate: '2026-09-04',
    topic: 'Fulfilling Covenants & Social Trust',
    khatibName: 'Imam Tariq Al-Banna',
    batchNumber: 2,
    khutbahTime: '14:00:00',
    language: 'English & Bengali',
    recordingUrl: 'https://www.youtube.com/watch?v=khutbah-2',
  },
];

export const MOCK_SUBMISSIONS: MosqueSubmission[] = [
  {
    id: 'sub-001',
    submitterId: 'usr-contributor-101',
    submitterEmail: 'ahmad.contributor@example.org',
    name: 'Al-Madinah Community Centre & Mosque',
    description: 'Newly converted facility serving worshippers in east suburb with daily prayers and weekend madrasah.',
    address: '45 Green Lane, Ilford',
    city: 'London',
    country: 'United Kingdom',
    latitude: 51.5589,
    longitude: 0.0812,
    contactPhone: '+44 20 8555 1290',
    websiteUrl: 'https://almadinah-ilford.org',
    facilityCodes: ['WUDU_AREA', 'WOMENS_SECTION', 'PARKING', 'WHEELCHAIR'],
    imageUrls: ['https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80'],
    status: 'PENDING',
    createdAt: '2026-09-10T15:30:00Z',
  },
  {
    id: 'sub-002',
    submitterId: 'usr-contributor-101',
    submitterEmail: 'ahmad.contributor@example.org',
    name: 'South London Islamic Center',
    description: 'Active community mosque with youth programs.',
    address: '12 Streatham High Rd',
    city: 'London',
    country: 'United Kingdom',
    latitude: 51.4289,
    longitude: -0.1312,
    facilityCodes: ['WUDU_AREA', 'PARKING'],
    imageUrls: ['https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=800&auto=format&fit=crop&q=80'],
    status: 'APPROVED',
    reviewComments: 'Address and facilities verified. Excellent submission.',
    createdAt: '2026-09-02T11:10:00Z',
  },
];

export const MOCK_CLAIMS: MosqueClaim[] = [
  {
    id: 'clm-001',
    mosqueId: '18558e48-a2aa-4219-9583-54cd9a84a76c',
    mosqueName: 'East London Mosque & London Muslim Centre',
    claimantId: 'usr-imam-202',
    claimantEmail: 'imam.tariq@eastlondonmosque.org',
    fullName: 'Imam Tariq Al-Banna',
    phoneNumber: '+44 20 7654 3210',
    officialEmail: 'imam.tariq@eastlondonmosque.org',
    positionInMosque: 'Head Imam & Senior Trustee',
    proofDocumentUrl: 'https://docs.openmosque.org/proofs/charity-commission-certificate.pdf',
    status: 'PENDING',
    createdAt: '2026-09-11T09:00:00Z',
  },
];

export const MOCK_FLAGS: ContentFlag[] = [
  {
    id: 'flg-1',
    targetType: 'REVIEW',
    targetId: 'rev-spam-101',
    reporterId: 'usr-contributor-101',
    reason: 'Commercial spam advertising unauthorized travel agency.',
    status: 'PENDING',
    createdAt: '2026-09-11T16:00:00Z',
  },
];

export const MOCK_ADMIN_USERS: User[] = [
  {
    id: 'usr-contributor-101',
    firebaseUid: 'mock-contributor-1',
    email: 'ahmad.contributor@example.org',
    displayName: 'Ahmad Al-Mansoor',
    role: 'USER',
    points: 180,
    isActive: true,
    isVerified: true,
    createdAt: '2026-01-15T10:00:00Z',
  },
  {
    id: 'usr-imam-202',
    firebaseUid: 'mock-imam-1',
    email: 'imam.tariq@eastlondonmosque.org',
    displayName: 'Imam Tariq Al-Banna',
    role: 'MOSQUE_ADMIN',
    points: 480,
    isActive: true,
    isVerified: true,
    createdAt: '2025-11-20T08:30:00Z',
  },
  {
    id: 'usr-moderator-303',
    firebaseUid: 'mock-moderator-1',
    email: 'fatima.moderator@openmosque.org',
    displayName: 'Fatima Zahra',
    role: 'MODERATOR',
    points: 820,
    isActive: true,
    isVerified: true,
    createdAt: '2025-08-10T14:15:00Z',
  },
  {
    id: 'usr-admin-404',
    firebaseUid: 'mock-admin-1',
    email: 'admin.zayd@openmosque.org',
    displayName: 'Zayd Ibn Haritha (Super Admin)',
    role: 'SUPER_ADMIN',
    points: 1540,
    isActive: true,
    isVerified: true,
    createdAt: '2025-01-01T00:00:00Z',
  },
];
