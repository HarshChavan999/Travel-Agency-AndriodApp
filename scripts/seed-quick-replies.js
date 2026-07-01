/**
 * Script to seed the app_config/global Firestore document with default quick replies.
 *
 * Usage:
 *   node scripts/seed-quick-replies.js
 *
 * Prerequisites:
 *   1. Ensure you are logged in: firebase login
 *   2. Or set GOOGLE_APPLICATION_CREDENTIALS env var pointing to your service account key
 *
 * Alternative (using env vars):
 *   FB_PROJECT_ID=... FB_CLIENT_EMAIL=... FB_PRIVATE_KEY="..." node scripts/seed-quick-replies.js
 */

const admin = require('firebase-admin');

const DEFAULT_BUYER_REPLIES = [
  "Is this package still available?",
  "Can you provide more details?",
  "Are dates flexible?",
  "Do you offer group discounts?"
];

const DEFAULT_SELLER_REPLIES = [
  "Yes, it's available. When are you planning to travel?",
  "Would you like me to send the complete itinerary?",
  "How many people are travelling?",
  "We have a special offer going on, would you like to hear about it?"
];

async function seedQuickReplies() {
  try {
    // Try service account from env vars first
    if (process.env.FB_PROJECT_ID && process.env.FB_CLIENT_EMAIL && process.env.FB_PRIVATE_KEY) {
      console.log('Using service account from environment variables...');
      admin.initializeApp({
        credential: admin.credential.cert({
          projectId: process.env.FB_PROJECT_ID,
          clientEmail: process.env.FB_CLIENT_EMAIL,
          privateKey: process.env.FB_PRIVATE_KEY.replace(/\\n/g, '\n'),
        }),
        projectId: process.env.FB_PROJECT_ID,
      });
    } else {
      console.log('Using Application Default Credentials...');
      admin.initializeApp();
    }

    const db = admin.firestore();

    console.log('Seeding quick replies to Firestore app_config/global...');

    await db.collection('app_config').doc('global').set({
      buyerQuickReplies: DEFAULT_BUYER_REPLIES,
      sellerQuickReplies: DEFAULT_SELLER_REPLIES,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedBy: 'seed-script'
    }, { merge: true });

    console.log(' Quick replies seeded successfully!');
    console.log('   Buyer replies (' + DEFAULT_BUYER_REPLIES.length + '):');
    DEFAULT_BUYER_REPLIES.forEach(r => console.log('      - ' + r));
    console.log('   Seller replies (' + DEFAULT_SELLER_REPLIES.length + '):');
    DEFAULT_SELLER_REPLIES.forEach(r => console.log('      - ' + r));
    console.log('\nAdmins can now edit these from:');
    console.log('   - WebApp:  Admin Settings > Quick Reply Messages');
    console.log('   - Android: Profile > Quick Reply Settings');

    process.exit(0);
  } catch (error) {
    console.error(' Error seeding quick replies:', error.message);
    console.error('\nMake sure you are logged in: firebase login');
    console.error('Or set GOOGLE_APPLICATION_CREDENTIALS to your service account key file.');
    process.exit(1);
  }
}

seedQuickReplies();
