/** Points at a local backend during development. Replace with the deployed
 * Render URL via an environment variable before shipping. */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8000';
