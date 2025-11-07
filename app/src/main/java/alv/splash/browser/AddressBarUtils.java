package alv.splash.browser;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

/**
 * Utility class to handle address bar toggle functionality
 */
public class AddressBarUtils {

    private boolean isAddressBarExpanded = false;
    private CardView addressBarContainer;
    private ImageView iconSecurity;
    private TextView txtPageTitle;

    /**
     * Constructor for AddressBarUtils
     *
     * @param addressBarContainer The container holding the address bar
     * @param iconSecurity The security icon
     * @param txtPageTitle The page title text view
     */
    public AddressBarUtils(CardView addressBarContainer, ImageView iconSecurity, TextView txtPageTitle) {
        this.addressBarContainer = addressBarContainer;
        this.iconSecurity = iconSecurity;
        this.txtPageTitle = txtPageTitle;
    }

    /**
     * Toggle between address bar and title bar views
     */
    public void toggleAddressBar() {
        if (isAddressBarExpanded) {
            collapseAddressBar();
        } else {
            expandAddressBar();
        }
    }

    /**
     * Expand the address bar (show address bar, hide title)
     */
    public void expandAddressBar() {
        if (isAddressBarExpanded) return;

        // Hide title and security icon with fade out
        animateVisibility(txtPageTitle, false, 150);
        animateVisibility(iconSecurity, false, 150);

        // Show address bar with fade in after a small delay
        animateVisibility(addressBarContainer, true, 300);

        isAddressBarExpanded = true;
    }

    /**
     * Collapse the address bar (hide address bar, show title)
     */
    public void collapseAddressBar() {
        if (!isAddressBarExpanded) return;

        // Hide address bar with fade out
        animateVisibility(addressBarContainer, false, 150);

        // Show title and security icon with fade in after a small delay
        animateVisibility(txtPageTitle, true, 300);
        animateVisibility(iconSecurity, true, 300);

        isAddressBarExpanded = false;
    }

    /**
     * Set the page title and update security icon based on HTTPS status
     *
     * @param title Page title
     * @param isSecure Whether the page is secure (HTTPS)
     */
    public void updatePageInfo(String title, boolean isSecure) {
        txtPageTitle.setText(title);
        iconSecurity.setImageResource(isSecure ? R.drawable.baseline_auto_awesome_24 : R.drawable.sharp_warning_24);
    }

    /**
     * Check if address bar is currently expanded
     *
     * @return true if expanded, false if collapsed
     */
    public boolean isExpanded() {
        return isAddressBarExpanded;
    }

    /**
     * Animate the visibility change of a view with fade
     *
     * @param view The view to animate
     * @param show True to show, false to hide
     * @param delay Animation delay in milliseconds
     */
    private void animateVisibility(View view, boolean show, long delay) {
        float startAlpha = show ? 0f : 1f;
        float endAlpha = show ? 1f : 0f;

        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", startAlpha, endAlpha);
        alpha.setDuration(250);
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet animSet = new AnimatorSet();
        animSet.play(alpha).after(delay);
        animSet.start();

        // Update visibility based on animation end state
        if (show) {
            view.setVisibility(View.VISIBLE);
        } else {
            alpha.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
        }
    }
}
