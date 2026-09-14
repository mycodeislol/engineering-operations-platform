import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: "class",
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "#08090C",
        surface: {
          DEFAULT: "#0E1015",
          elevated: "#151821",
          hover: "#1C202C",
        },
        border: {
          subtle: "#1C202C",
          DEFAULT: "#262B3B",
          strong: "#3B4259",
          glow: "rgba(100, 116, 139, 0.2)",
        },
        accent: {
          cyan: "#00F0FF",
          emerald: "#10B981",
          amber: "#F59E0B",
          rose: "#F43F5E",
          indigo: "#6366F1",
        },
        text: {
          primary: "#F8FAFC",
          secondary: "#94A3B8",
          muted: "#64748B",
        },
      },
      fontFamily: {
        sans: [
          "var(--font-geist-sans)",
          "-apple-system",
          "BlinkMacSystemFont",
          "Segoe UI",
          "Roboto",
          "sans-serif",
        ],
        mono: ["var(--font-geist-mono)", "Menlo", "Courier New", "monospace"],
      },
      letterSpacing: {
        tightest: "-0.035em",
        tighter: "-0.02em",
        tight: "-0.01em",
        wideBadge: "0.06em",
      },
      boxShadow: {
        "bento-glow": "0 0 0 1px rgba(255, 255, 255, 0.05), 0 1px 2px 0 rgba(0, 0, 0, 0.4)",
        "bento-hover": "0 0 0 1px rgba(99, 102, 241, 0.3), 0 8px 24px -4px rgba(0, 0, 0, 0.6)",
        "incident-pulse": "0 0 20px -2px rgba(244, 63, 94, 0.35)",
        "glass-inner": "inset 0 1px 1px 0 rgba(255, 255, 255, 0.08)",
      },
      animation: {
        "pulse-subtle": "pulseSubtle 3s cubic-bezier(0.4, 0, 0.6, 1) infinite",
        "border-shine": "borderShine 6s linear infinite",
      },
      keyframes: {
        pulseSubtle: {
          "0%, 100%": { opacity: "1" },
          "50%": { opacity: "0.6" },
        },
        borderShine: {
          "0%": { backgroundPosition: "0% 50%" },
          "50%": { backgroundPosition: "100% 50%" },
          "100%": { backgroundPosition: "0% 50%" },
        },
      },
    },
  },
  plugins: [],
};

export default config;
