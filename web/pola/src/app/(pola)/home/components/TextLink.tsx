import { ArrowRight } from "lucide-react";
import Link from "next/link";

interface TextLinkProps {
  text: string;
  link: string;
}

export default function TextLink({ text, link }: TextLinkProps) {
  return (
    <Link
      href={link}
      className="flex items-center justify-start gap-2 mb-4 text-2xl font-bold text-[#4C3D25] hover:underline hover:opacity-80 transition-all w-4/5"
    >
      <span>{text}</span>
      <ArrowRight className="w-5 h-5" />
    </Link>
  );
}
