import Image from "next/image";

export default function LandingPage() {
  return (
    <main className="flex flex-col items-center justify-center min-h-screen bg-[#FFFEF8]">
      <Image
        src="/image/POLA_logo_1.png"
        alt="Pola Logo"
        width={360}
        height={360}
        priority
      />

      <h1 className="text-4xl font-semibold text-[#4C3D25] mt-8 mb-6">
        나만의 스크랩북, POLA
      </h1>

      <a
        href="/home" // TODO: 우선 홈 화면으로 연결, 이후 구글 OAuth 연결
        className="flex items-center gap-3 border border-gray-300 rounded-md px-6 py-2 bg-white shadow-sm hover:bg-gray-50 transition"
      >
        <Image
          src="https://developers.google.com/identity/images/g-logo.png"
          alt="Google logo"
          width={20}
          height={20}
        />
        <span className="text-sm text-gray-600 font-medium">
          Sign in with Google
        </span>
      </a>
    </main>
  );
}
