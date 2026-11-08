document.addEventListener("DOMContentLoaded", () => {
    // Parallax effect on ambient glows with mouse movement
    const glow1 = document.querySelector(".glow-top-right");
    const glow2 = document.querySelector(".glow-bottom-left");

    window.addEventListener("mousemove", (e) => {
        const x = (e.clientX / window.innerWidth) * 30;
        const y = (e.clientY / window.innerHeight) * 30;
        if (glow1) glow1.style.transform = `translate(${x}px, ${y}px)`;
        if (glow2) glow2.style.transform = `translate(${-x}px, ${-y}px)`;
    });
});