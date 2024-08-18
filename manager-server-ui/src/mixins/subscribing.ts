export default {
  created() {
    this.subscribe();
  },
  beforeUnmount() {
    this.unsubscribe();
  },
  methods: {
    async subscribe() {
      if (!this.subscription) {
        this.subscription = await this.createSubscription();
      }
    },
    unsubscribe() {
      if (this.subscription && !this.subscription.closed) {
        try {
          this.subscription.unsubscribe();
        } finally {
          this.subscription = null;
        }
      }
    },
  },
};
