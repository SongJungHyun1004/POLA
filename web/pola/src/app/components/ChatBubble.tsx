"use client";

interface ChatBubbleProps {
  role: "user" | "ai";
  text: string;
}

export default function ChatBubble({ role, text }: ChatBubbleProps) {
  const isUser = role === "user";
  return (
    <div className={`flex ${isUser ? "justify-end" : "justify-start"}`}>
      <div
        className={`max-w-[70%] px-4 py-3 rounded-2xl text-sm leading-relaxed shadow-sm ${
          isUser
            ? "bg-[#4C3D25] text-white rounded-br-none"
            : "bg-[#FFFEF8] border border-[#D2C9B0] text-[#4C3D25] rounded-bl-none"
        }`}
      >
        {text}
      </div>
    </div>
  );
}
