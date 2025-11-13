"use client";

import { Send } from "lucide-react";
import { useState } from "react";

interface ChatInputProps {
  placeholder?: string;
  onSubmit?: (value: string) => void;
}

export default function ChatInput({ placeholder, onSubmit }: ChatInputProps) {
  const [value, setValue] = useState("");

  const handleSend = () => {
    if (!value.trim()) return;
    onSubmit?.(value);
    setValue("");
  };

  return (
    <div className="flex items-center bg-[#FFFEF8] border border-[#D2C9B0] rounded-full px-4 py-2">
      <input
        type="text"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder={placeholder || "메시지를 입력하세요"}
        onKeyDown={(e) => e.key === "Enter" && handleSend()}
        className="flex-grow bg-transparent outline-none text-sm text-[#4C3D25]"
      />
      <button
        onClick={handleSend}
        className="ml-2 text-[#4C3D25] hover:text-black"
      >
        <Send className="w-4 h-4" />
      </button>
    </div>
  );
}
